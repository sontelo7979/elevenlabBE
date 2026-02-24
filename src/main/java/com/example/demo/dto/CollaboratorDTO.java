package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CollaboratorDTO {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private Double commissionRate;
    private Double totalCommission;
    private String phoneNumber;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
    private String notes;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}