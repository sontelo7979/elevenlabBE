// DeviceController.java
package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.DeviceRegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device")
public class DeviceController {
    private final DeviceRegistrationService deviceRegistrationService;

    public DeviceController(DeviceRegistrationService deviceRegistrationService) {
        this.deviceRegistrationService = deviceRegistrationService;
    }

    // API để admin thay đổi thiết bị đăng ký
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/change")
    public ResponseEntity<ApiResponse<String>> changeDevice(
            @RequestParam Long userId,
            @RequestParam String newDeviceId,
            @RequestParam String adminCode) {
        try {
            boolean success = deviceRegistrationService.changeRegisteredDevice(userId, newDeviceId, adminCode);
            if (success) {
                ApiResponse<String> response = new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Đã thay đổi thiết bị đăng ký thành công"
                );
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<String> response = new ApiResponse<>(
                        HttpStatus.BAD_REQUEST.value(),
                        "Không thể thay đổi thiết bị"
                );
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lỗi: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API để lấy thông tin thiết bị đã đăng ký
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/info/{userId}")
    public ResponseEntity<ApiResponse<String>> getDeviceInfo(@PathVariable Long userId) {
        try {
            String deviceId = deviceRegistrationService.getRegisteredDeviceId(userId);
            if (deviceId != null) {
                ApiResponse<String> response = new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Thiết bị đã đăng ký: " + deviceId,
                        deviceId
                );
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<String> response = new ApiResponse<>(
                        HttpStatus.NOT_FOUND.value(),
                        "Không tìm thấy thông tin thiết bị cho user ID: " + userId
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Lỗi: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}