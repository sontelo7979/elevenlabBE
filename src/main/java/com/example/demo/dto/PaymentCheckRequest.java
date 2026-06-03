// PaymentCheckRequest.java
package com.example.demo.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentCheckRequest {
    private String content;        // Nội dung chuyển khoản
    private BigDecimal amount;     // Số tiền
    private String customerEmail;  // Email người dùng (để lưu vào DB)
    private String customerName;   // Tên người dùng
    private String packageName;    // Tên gói thanh toán (để lưu vào DB)
}