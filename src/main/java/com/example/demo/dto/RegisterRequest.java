package com.example.demo.dto;
import java.time.LocalDateTime;
import java.util.Set; // Thêm dòng này

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private Set<Integer> roles;
    private Set<String> permissions; // Thêm field permissions
    private LocalDateTime startDate; // Thêm cho CUSTOMER
    private LocalDateTime endDate;   // Thêm cho CUSTOMER
}