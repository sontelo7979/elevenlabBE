package com.example.demo.service;

import com.example.demo.dto.CustomerDTO;
import com.example.demo.model.Customer;
import com.example.demo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Lấy thông tin customer theo email
     * Nếu không tìm thấy thì TỰ ĐỘNG TẠO MỚI
     *
     * @param email Email của customer
     * @param name Tên customer (có thể null, nếu null sẽ lấy từ email)
     * @return CustomerDTO
     */
    @Transactional
    public CustomerDTO getOrCreateCustomerByEmail(String email, String name) {
        log.info("Getting or creating customer by email: {}, name: {}", email, name);

        // Tìm customer theo email
        Optional<Customer> existingCustomer = customerRepository.findByEmailIgnoreCase(email);

        if (existingCustomer.isPresent()) {
            log.info("Found existing customer: {}", email);
            return convertToDTO(existingCustomer.get());
        } else {
            // Không tìm thấy -> tạo mới
            log.info("Customer not found, creating new customer: {}", email);

            String customerName = (name != null && !name.trim().isEmpty()) ? name : extractNameFromEmail(email);

            Customer newCustomer = Customer.builder()
                    .name(customerName)
                    .email(email.toLowerCase())
                    .startDate(null)
                    .endDate(null)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Customer saved = customerRepository.save(newCustomer);
            log.info("Created new customer: id={}, email={}, name={}", saved.getId(), saved.getEmail(), saved.getName());

            return convertToDTO(saved);
        }
    }

    /**
     * Lấy thông tin customer theo email (chỉ tìm, không tạo mới)
     * @param email Email của customer
     * @return CustomerDTO
     * @throws RuntimeException nếu không tìm thấy
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerByEmail(String email) {
        log.info("Getting customer by email: {}", email);

        Customer customer = customerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy customer với email: " + email));

        return convertToDTO(customer);
    }

    /**
     * Lấy thông tin customer theo email (không throw exception, trả về optional)
     */
    @Transactional(readOnly = true)
    public Optional<CustomerDTO> findCustomerByEmail(String email) {
        log.info("Finding customer by email: {}", email);

        return customerRepository.findByEmailIgnoreCase(email)
                .map(this::convertToDTO);
    }

    /**
     * Kiểm tra customer có tồn tại không
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    /**
     * Chuyển đổi Customer -> CustomerDTO
     */
    private CustomerDTO convertToDTO(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .startDate(customer.getStartDate())
                .endDate(customer.getEndDate())
                .isActive(customer.getIsActive())
                .isValid(customer.isValid())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    /**
     * Trích xuất tên từ email (phần trước @)
     * Ví dụ: vn.napthe24h@gmail.com -> vn.napthe24h
     */
    private String extractNameFromEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "Unknown";
        }
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            String name = email.substring(0, atIndex);
            // Thay thế dấu chấm và các ký tự đặc biệt bằng khoảng trắng
            name = name.replace(".", " ").replace("_", " ");
            // Viết hoa chữ cái đầu mỗi từ
            String[] parts = name.split(" ");
            StringBuilder result = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty()) {
                    result.append(Character.toUpperCase(part.charAt(0)))
                            .append(part.substring(1).toLowerCase())
                            .append(" ");
                }
            }
            return result.toString().trim();
        }
        return email;
    }
}