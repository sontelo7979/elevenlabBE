package com.example.demo.dto;

import lombok.Data;

@Data
public class AdminDeviceUpdateRequest {
    private String fingerprint;
    private String action; // "approve" hoặc "reject"
}
