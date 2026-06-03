package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerDTO {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Boolean isValid;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}