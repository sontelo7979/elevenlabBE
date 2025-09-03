package com.example.demo.dto;

import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String token;
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private List<String> permissions; // Thêm field permissions
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private String registeredDeviceId;
    private LocalDateTime deviceRegisteredAt;
}