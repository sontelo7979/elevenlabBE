package com.example.demo.dto;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplyKeyRequest {
    private Long userId;
    private String keyCode;
}