package com.example.demo.service;

import com.example.demo.config.SepayConfig;
import com.example.demo.dto.SepayTransactionResponse;
import com.example.demo.model.CustomerTransaction;
import com.example.demo.model.TransactionStatus;
import com.example.demo.repository.CustomerTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SepayService {

    private final SepayConfig sepayConfig;
    private final CustomerTransactionRepository transactionRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter SEPAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * Lấy danh sách giao dịch từ Sepay API
     */
    public List<SepayTransactionResponse.TransactionData> getTransactions(LocalDateTime fromDate, LocalDateTime toDate) {
        String url = buildSepayUrl(fromDate, toDate);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("Calling Sepay API - attempt {}/{}: {}", attempt, MAX_RETRIES, url);

                HttpHeaders headers = buildHeaders();
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    SepayTransactionResponse transactionResponse = objectMapper.readValue(
                            response.getBody(), SepayTransactionResponse.class);

                    if (transactionResponse.getStatus() != null && transactionResponse.getStatus() == 200) {
                        List<SepayTransactionResponse.TransactionData> transactions =
                                transactionResponse.getTransactions() != null ?
                                        transactionResponse.getTransactions() : new ArrayList<>();

                        log.info("Successfully fetched {} transactions from Sepay", transactions.size());

                        // Log chi tiết từng giao dịch để debug
                        for (SepayTransactionResponse.TransactionData tx : transactions) {
                            log.debug("Sepay tx: id={}, content={}, amountIn={}, date={}",
                                    tx.getId(), tx.getTransactionContent(), tx.getAmountIn(), tx.getTransactionDate());
                        }

                        return transactions;
                    } else {
                        log.warn("Sepay API returned error status: {}", transactionResponse.getStatus());
                    }
                }
            } catch (Exception e) {
                log.error("Error calling Sepay API - attempt {}/{}: {}", attempt, MAX_RETRIES, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        log.error("Failed to fetch transactions after {} attempts", MAX_RETRIES);
        return new ArrayList<>();
    }

    /**
     * Kiểm tra thanh toán - CHỈ CẦN NỘI DUNG CHỨA (CONTAINS)
     *
     * @param content Nội dung cần tìm (VD: "BASIC", "PREMIUM", "VIP")
     * @param amount Số tiền cần khớp
     * @param fromDate Thời gian bắt đầu quét
     * @param toDate Thời gian kết thúc quét
     * @return Optional chứa giao dịch nếu tìm thấy
     */
    public Optional<SepayTransactionResponse.TransactionData> checkPayment(
            String content,
            BigDecimal amount,
            LocalDateTime fromDate,
            LocalDateTime toDate) {

        log.info("Checking payment - content contains: '{}', amount: {}", content, amount);

        List<SepayTransactionResponse.TransactionData> transactions = getTransactions(fromDate, toDate);

        if (transactions.isEmpty()) {
            log.info("No transactions found in period");
            return Optional.empty();
        }

        // Tìm giao dịch có nội dung CHỨA content (không phân biệt hoa thường)
        Optional<SepayTransactionResponse.TransactionData> match = transactions.stream()
                .filter(t -> t.getTransactionContent() != null &&
                        t.getTransactionContent().toLowerCase().contains(content.toLowerCase()))
                .filter(t -> t.getAmountIn() != null &&
                        t.getAmountIn().compareTo(amount) == 0)
                .findFirst();

        if (match.isPresent()) {
            log.info("Found matching transaction (contains): id={}, sepayId={}, content={}",
                    match.get().getId(), match.get().getId(), match.get().getTransactionContent());
            return match;
        }

        log.info("No matching transaction found for content containing: '{}' and amount: {}", content, amount);
        return Optional.empty();
    }

    /**
     * LƯU TRANSACTION - CHỈ 1 LẦN DUY NHẤT
     *
     * @param sepayTransaction Giao dịch từ Sepay
     * @param transaction Giao dịch trong DB cần cập nhật
     * @return true nếu lưu thành công (lần đầu), false nếu đã tồn tại hoặc không thể lưu
     */
    @Transactional
    public boolean saveTransactionIfNotExists(SepayTransactionResponse.TransactionData sepayTransaction,
                                              CustomerTransaction transaction) {
        try {
            String sepayId = sepayTransaction.getId();
            log.info("Attempting to save transaction - sepayId: {}, localTxId: {}", sepayId, transaction.getId());

            // Bước 1: KIỂM TRA NGHIÊM NGẶT - Đã có transaction SUCCESS với sepayId này chưa?
            boolean alreadyExists = transactionRepository.existsBySepayTransactionIdAndStatus(
                    sepayId, TransactionStatus.SUCCESS);

            if (alreadyExists) {
                log.warn("❌ TRANSACTION ALREADY EXISTS IN DB! sepayId: {} - WILL NOT UPDATE AGAIN", sepayId);
                return false;
            }

            // Bước 2: Kiểm tra transaction có đang ở trạng thái PENDING không
            if (transaction.getStatus() != TransactionStatus.PENDING) {
                log.warn("❌ Transaction {} is not PENDING (current status: {}), cannot update",
                        transaction.getId(), transaction.getStatus());
                return false;
            }

            // Bước 3: Kiểm tra optimistic lock - đảm bảo chưa bị cập nhật bởi request khác
            Optional<CustomerTransaction> freshTransaction = transactionRepository.findById(transaction.getId());
            if (freshTransaction.isEmpty() || freshTransaction.get().getStatus() != TransactionStatus.PENDING) {
                log.warn("❌ Transaction {} has been already updated by another request", transaction.getId());
                return false;
            }

            // Bước 4: Thực hiện update (chỉ update nếu còn PENDING)
            int updated = transactionRepository.updatePendingTransaction(
                    transaction.getId(),
                    TransactionStatus.SUCCESS,
                    sepayId,  // transactionId cũng là sepayId
                    sepayTransaction.getTransactionDate(),
                    sepayTransaction.getBankBrandName(),
                    sepayTransaction.getAccountNumber(),
                    sepayTransaction.getAmountOut() != null &&
                            sepayTransaction.getAmountOut().compareTo(BigDecimal.ZERO) > 0 ? "OUT" : "IN",
                    sepayId,  // sepayTransactionId
                    LocalDateTime.now()
            );

            // Bước 5: Kiểm tra kết quả update
            if (updated > 0) {
                log.info("✅ SUCCESSFULLY SAVED TRANSACTION (FIRST TIME ONLY!): localId={}, sepayId={}, amount={}",
                        transaction.getId(), sepayId, sepayTransaction.getAmountIn());
                return true;
            } else {
                log.warn("⚠️ Transaction {} could not be updated (may have been updated by another request)",
                        transaction.getId());

                // Kiểm tra lại xem có ai đã update chưa
                boolean nowExists = transactionRepository.existsBySepayTransactionIdAndStatus(sepayId, TransactionStatus.SUCCESS);
                if (nowExists) {
                    log.info("Transaction {} was already updated by another concurrent request", sepayId);
                }
                return false;
            }

        } catch (Exception e) {
            log.error("❌ Error saving transaction: sepayId={}, error={}",
                    sepayTransaction.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Kiểm tra kết nối đến Sepay API
     */
    public boolean testConnection() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime fiveMinutesAgo = now.minusMinutes(5);
            List<SepayTransactionResponse.TransactionData> transactions = getTransactions(fiveMinutesAgo, now);
            return transactions != null;
        } catch (Exception e) {
            log.error("Sepay API connection test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Xây dựng URL gọi API Sepay
     */
    private String buildSepayUrl(LocalDateTime fromDate, LocalDateTime toDate) {
        String fromDateStr = fromDate != null ? fromDate.format(SEPAY_DATE_FORMAT) : "";
        String toDateStr = toDate != null ? toDate.format(SEPAY_DATE_FORMAT) : "";

        return String.format("%s/transactions/list?transaction_date_min=%s&transaction_date_max=%s",
                sepayConfig.getApiUrl(),
                fromDateStr,
                toDateStr);
    }

    /**
     * Xây dựng headers cho request
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(sepayConfig.getApiToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}