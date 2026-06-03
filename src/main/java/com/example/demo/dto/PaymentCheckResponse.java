package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentCheckResponse {
    private boolean paid;
    private boolean firstTime;  // Thêm flag này: true = lần đầu thanh toán
    private String message;
    private LocalDateTime paidAt;
}