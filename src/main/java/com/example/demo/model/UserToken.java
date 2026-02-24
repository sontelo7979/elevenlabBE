package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_tokens")
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "token_type", length = 50)
    private String tokenType = "BEARER";

    @Column(name = "device_id", length = 255)
    private String deviceId; // Thiết bị đăng nhập

    @Column(name = "ip_address", length = 50)
    private String ipAddress; // Địa chỉ IP

    @Column(name = "user_agent", length = 500)
    private String userAgent; // Thông tin trình duyệt

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "is_valid")
    private Boolean isValid = true;

    @Column(name = "logout_at")
    private LocalDateTime logoutAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}