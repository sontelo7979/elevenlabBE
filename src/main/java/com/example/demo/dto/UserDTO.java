package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Boolean isAccountValid;
    private String registeredDeviceId;
    private LocalDateTime deviceRegisteredAt;
    private Set<String> roles;
    private List<String> permissions;

    // Thông tin về CTV đã đăng ký
    private Long registeredByCollaboratorId;
    private String registeredByCollaboratorUsername;
    private String registeredByCollaboratorEmail;

    // Thống kê
    private Long totalRenewals;
    private LocalDateTime lastRenewalDate;
    private LocalDateTime firstRegisteredDate;
}