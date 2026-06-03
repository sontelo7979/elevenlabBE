package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @Column(name = "package_name")
    private String packageName;  // Thêm trường này

    @Column(name = "transaction_id", unique = true)
    private String transactionId; // Mã giao dịch từ Sepay

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "content", length = 500)
    private String content; // Nội dung chuyển khoản

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "sepay_transaction_id")
    private String sepayTransactionId; // ID giao dịch từ Sepay

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}