package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PaymentCheckRequest;
import com.example.demo.dto.PaymentCheckResponse;
import com.example.demo.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<PaymentCheckResponse>> checkPayment(
            @RequestBody PaymentCheckRequest request) {

        PaymentCheckResponse response = paymentService.checkAndSavePayment(request);

        // Trả về status code khác nhau dựa vào kết quả
        if (response.isPaid()) {
            if (response.isFirstTime()) {
                // Lần đầu thanh toán thành công
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse<>(200, "Thanh toán thành công (lần đầu)", response));
            } else {
                // Đã thanh toán trước đó
                return ResponseEntity.status(HttpStatus.ALREADY_REPORTED) // 208
                        .body(new ApiResponse<>(208, "Đã thanh toán trước đó, không xử lý lại", response));
            }
        } else {
            // Chưa thanh toán
            return ResponseEntity.status(HttpStatus.ACCEPTED) // 202
                    .body(new ApiResponse<>(202, "Chưa nhận được thanh toán", response));
        }
    }
}