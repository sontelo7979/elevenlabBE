package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_subscription_keys")
@Data
public class UserSubscriptionKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User là khách hàng (người được đăng ký/gia hạn)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "key_id", nullable = false)
    private SubscriptionKey subscriptionKey;

    // Lưu userId của collaborator (người thực hiện đăng ký/gia hạn)
    @Column(name = "collaborator_user_id")
    private Long collaboratorUserId;

    @Column(nullable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime newEndDate; // Ngày hết hạn mới sau khi áp dụng
}