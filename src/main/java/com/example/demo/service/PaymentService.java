package com.example.demo.service;

import com.example.demo.dto.PaymentCheckRequest;
import com.example.demo.dto.PaymentCheckResponse;
import com.example.demo.dto.SepayTransactionResponse;
import com.example.demo.model.Customer;
import com.example.demo.model.CustomerTransaction;
import com.example.demo.model.TransactionStatus;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.CustomerTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final CustomerRepository customerRepository;
    private final CustomerTransactionRepository transactionRepository;
    private final SepayService sepayService;

    private static final int SCAN_MINUTES = 30;

    // Cấu hình số tháng theo số tiền
    private static final BigDecimal AMOUNT_1_MONTH = new BigDecimal("50000");
    private static final BigDecimal AMOUNT_3_MONTH = new BigDecimal("120000");
    private static final BigDecimal AMOUNT_6_MONTH = new BigDecimal("200000");

    @Transactional
    public PaymentCheckResponse checkAndSavePayment(PaymentCheckRequest request) {
        // 1. Kiểm tra đã thanh toán thành công trong DB chưa
        Optional<CustomerTransaction> existingSuccess = transactionRepository
                .findSuccessByContentContaining(request.getContent(), request.getAmount());

        if (existingSuccess.isPresent()) {
            log.info("Payment already succeeded for content containing: {}", request.getContent());
            return PaymentCheckResponse.builder()
                    .paid(true)
                    .firstTime(false)
                    .message("Đã thanh toán thành công trước đó (không xử lý lại)")
                    .paidAt(existingSuccess.get().getTransactionDate())
                    .build();
        }

        // 2. Kiểm tra pending transaction
        Optional<CustomerTransaction> pending = transactionRepository
                .findPendingByContentContaining(request.getContent(), request.getAmount());

        CustomerTransaction transaction;
        if (pending.isPresent()) {
            transaction = pending.get();
            log.info("Found pending transaction: {}", transaction.getId());
        } else {
            // 3. Tạo mới pending transaction
            transaction = createNewTransaction(request);
            log.info("Created new pending transaction: {}", transaction.getId());
        }

        // 4. Gọi Sepay API quét giao dịch
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.minusMinutes(SCAN_MINUTES);

        Optional<SepayTransactionResponse.TransactionData> sepayTx =
                sepayService.checkPayment(
                        request.getContent(),
                        request.getAmount(),
                        fromDate,
                        now
                );

        // 5. Nếu tìm thấy -> cập nhật thành công và gia hạn endDate
        if (sepayTx.isPresent()) {
            // Kiểm tra lại lần nữa (tránh race condition)
            boolean alreadySuccess = transactionRepository.existsBySepayTransactionIdAndStatus(
                    sepayTx.get().getId(), TransactionStatus.SUCCESS);

            if (alreadySuccess) {
                log.warn("Transaction {} already succeeded in DB, skipping update", sepayTx.get().getId());
                return PaymentCheckResponse.builder()
                        .paid(true)
                        .firstTime(false)
                        .message("Đã thanh toán thành công trước đó")
                        .paidAt(sepayTx.get().getTransactionDate())
                        .build();
            }

            // Lưu transaction thành công
            boolean updated = sepayService.saveTransactionIfNotExists(sepayTx.get(), transaction);

            if (updated) {
                // CẬP NHẬT ENDDATE CHO CUSTOMER DỰA TRÊN SỐ TIỀN
                updateCustomerEndDate(transaction.getCustomer(), request.getAmount());

                log.info("FIRST TIME - Payment confirmed for transaction: {}, sepayId: {}",
                        transaction.getId(), sepayTx.get().getId());
                return PaymentCheckResponse.builder()
                        .paid(true)
                        .firstTime(true)
                        .message("Thanh toán thành công! Đã gia hạn gói dịch vụ")
                        .paidAt(sepayTx.get().getTransactionDate())
                        .build();
            } else {
                // Kiểm tra lại DB
                Optional<CustomerTransaction> checkAgain = transactionRepository
                        .findSuccessByContentContaining(request.getContent(), request.getAmount());
                if (checkAgain.isPresent()) {
                    return PaymentCheckResponse.builder()
                            .paid(true)
                            .firstTime(false)
                            .message("Đã thanh toán thành công")
                            .paidAt(checkAgain.get().getTransactionDate())
                            .build();
                }
                return PaymentCheckResponse.builder()
                        .paid(false)
                        .firstTime(false)
                        .message("Có lỗi xảy ra, vui lòng thử lại")
                        .build();
            }
        }

        // 6. Chưa tìm thấy
        log.info("No payment found for content: {}", request.getContent());
        return PaymentCheckResponse.builder()
                .paid(false)
                .firstTime(false)
                .message("Chưa phát hiện thanh toán, vui lòng thử lại sau")
                .build();
    }

    /**
     * Cập nhật endDate cho customer dựa trên số tiền thanh toán
     */
    private void updateCustomerEndDate(Customer customer, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newEndDate;

        // Xác định số tháng cần cộng thêm
        int monthsToAdd = getMonthsByAmount(amount);

        if (monthsToAdd == 0) {
            log.warn("Unknown amount: {} for customer: {}, no update applied", amount, customer.getEmail());
            return;
        }

        // Nếu endDate hiện tại null hoặc đã hết hạn, tính từ thời điểm hiện tại
        if (customer.getEndDate() == null || customer.getEndDate().isBefore(now)) {
            newEndDate = now.plusMonths(monthsToAdd);
            log.info("Customer {} has no valid endDate, setting new endDate from now: {}",
                    customer.getEmail(), newEndDate);
        } else {
            // Nếu còn hạn, cộng dồn thêm tháng
            newEndDate = customer.getEndDate().plusMonths(monthsToAdd);
            log.info("Customer {} existing endDate: {}, adding {} months, new endDate: {}",
                    customer.getEmail(), customer.getEndDate(), monthsToAdd, newEndDate);
        }

        // Nếu startDate null thì set từ thời điểm hiện tại
        if (customer.getStartDate() == null) {
            customer.setStartDate(now);
        }

        customer.setEndDate(newEndDate);
        customer.setUpdatedAt(now);
        customerRepository.save(customer);

        log.info("Updated customer {}: startDate={}, endDate={}, amount={}, monthsAdded={}",
                customer.getEmail(), customer.getStartDate(), newEndDate, amount, monthsToAdd);
    }

    /**
     * Xác định số tháng cần cộng dựa vào số tiền
     */
    private int getMonthsByAmount(BigDecimal amount) {
        if (amount.compareTo(AMOUNT_6_MONTH) == 0) {
            return 6;  // 200,000 VND -> 6 tháng
        } else if (amount.compareTo(AMOUNT_3_MONTH) == 0) {
            return 3;  // 120,000 VND -> 3 tháng
        } else if (amount.compareTo(AMOUNT_1_MONTH) == 0) {
            return 1;  // 50,000 VND -> 1 tháng
        } else {
            log.warn("Unknown amount: {}, no matching package", amount);
            return 0;
        }
    }

    private CustomerTransaction createNewTransaction(PaymentCheckRequest request) {
        // Tìm hoặc tạo Customer
        Customer customer = customerRepository.findByEmail(request.getCustomerEmail())
                .orElseGet(() -> {
                    Customer newCustomer = Customer.builder()
                            .name(request.getCustomerName())
                            .email(request.getCustomerEmail())
                            .isActive(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    log.info("Created new customer: {}", newCustomer.getEmail());
                    return customerRepository.save(newCustomer);
                });

        // Tạo transaction mới
        CustomerTransaction transaction = CustomerTransaction.builder()
                .customer(customer)
                .packageName(request.getPackageName())
                .amount(request.getAmount())
                .content(request.getContent())
                .status(TransactionStatus.PENDING)
                .transactionDate(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }
}