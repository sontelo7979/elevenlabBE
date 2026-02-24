package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserFilterDTO {
    private String username;
    private String email;
    private Boolean isActive;
    private Boolean isAccountValid;
    private LocalDateTime startDateFrom;
    private LocalDateTime startDateTo;
    private LocalDateTime endDateFrom;
    private LocalDateTime endDateTo;
    private Long registeredByCollaboratorId;
    private String role;
}