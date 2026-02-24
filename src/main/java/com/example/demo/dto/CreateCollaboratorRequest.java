package com.example.demo.dto;

import lombok.Data;

@Data
public class CreateCollaboratorRequest {
    private Long userId; // ID của user sẽ được làm CTV
    private Double commissionRate; // Tỷ lệ hoa hồng (mặc định 10% nếu không gửi)
    private String phoneNumber;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
    private String notes;
}