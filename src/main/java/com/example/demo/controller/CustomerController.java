package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CustomerDTO;
import com.example.demo.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    /**
     * API lấy thông tin customer theo email
     * Nếu không tìm thấy thì TỰ ĐỘNG TẠO MỚI
     * GET /api/customers?email=xxx&name=xxx (name là optional)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CustomerDTO>> getOrCreateCustomerByEmail(
            @RequestParam String email,
            @RequestParam(required = false) String name) {

        log.info("REST request to get or create customer by email: {}, name: {}", email, name);

        try {
            CustomerDTO customer = customerService.getOrCreateCustomerByEmail(email, name);

            // Kiểm tra xem customer vừa tạo hay đã có từ trước
            boolean isNew = customer.getCreatedAt().equals(customer.getUpdatedAt());

            return ResponseEntity.ok(new ApiResponse<>(
                    HttpStatus.OK.value(),
                    isNew ? "Tạo mới customer thành công" : "Lấy thông tin customer thành công",
                    customer
            ));
        } catch (Exception e) {
            log.error("Error getting/creating customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Có lỗi xảy ra: " + e.getMessage(),
                            null
                    ));
        }
    }

    /**
     * API lấy thông tin customer theo email (chỉ tìm, không tạo mới)
     * GET /api/customers/find?email=xxx
     */
    @GetMapping("/find")
    public ResponseEntity<ApiResponse<CustomerDTO>> findCustomerByEmail(
            @RequestParam String email) {

        log.info("REST request to find customer by email: {}", email);

        try {
            CustomerDTO customer = customerService.getCustomerByEmail(email);
            return ResponseEntity.ok(new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy thông tin customer thành công",
                    customer
            ));
        } catch (Exception e) {
            log.error("Error finding customer by email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(
                            HttpStatus.NOT_FOUND.value(),
                            e.getMessage(),
                            null
                    ));
        }
    }

    /**
     * API kiểm tra customer tồn tại
     * GET /api/customers/exists?email=xxx
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkCustomerExists(
            @RequestParam String email) {

        log.info("REST request to check customer exists: {}", email);

        boolean exists = customerService.existsByEmail(email);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                exists ? "Customer tồn tại" : "Customer không tồn tại",
                exists
        ));
    }
}