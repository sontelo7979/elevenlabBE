package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_keys")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // Ví dụ: "1MONTH", "3MONTHS", "1YEAR"

    @Column(nullable = false)
    private String description; // Mô tả: "Gia hạn 1 tháng"

    @Column(nullable = false)
    private Duration duration; // Thời gian gia hạn

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean isUsed = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}