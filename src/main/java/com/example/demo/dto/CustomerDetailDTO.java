package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CustomerDetailDTO {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Boolean isValid;
    private Double totalPaid;
    private Long totalTransactions;
    private List<TransactionDTO> transactions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class TransactionDTO {
        private Long id;
        private BigDecimal amount;
        private String content;
        private String packageName;
        private String status;
        private LocalDateTime transactionDate;
        private String bankCode;
    }
}