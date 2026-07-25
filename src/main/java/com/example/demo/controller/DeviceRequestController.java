package com.example.demo.controller;

import com.example.demo.dto.AdminDeviceUpdateRequest;
import com.example.demo.dto.DeviceRequestDTO;
import com.example.demo.dto.DeviceStatusResponse;
import com.example.demo.model.DeviceRequest;
import com.example.demo.service.DeviceRequestService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class DeviceRequestController {

    private final DeviceRequestService deviceRequestService;

    public DeviceRequestController(DeviceRequestService deviceRequestService) {
        this.deviceRequestService = deviceRequestService;
    }

    /**
     * API 1 – Gửi yêu cầu kích hoạt thiết bị
     * POST /api/v1/device/request
     * Public endpoint - không cần xác thực
     */
    @PostMapping("/device/request")
    public ResponseEntity<DeviceStatusResponse> submitDeviceRequest(@RequestBody DeviceRequestDTO request) {
        if (request.getFingerprint() == null || request.getFingerprint().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new DeviceStatusResponse("error", "Fingerprint không được để trống."));
        }

        DeviceStatusResponse response = deviceRequestService.submitDeviceRequest(request.getFingerprint());

        // Xác định HTTP status code dựa trên trạng thái
        HttpStatus httpStatus = switch (response.getStatus()) {
            case "approved" -> HttpStatus.OK;
            case "rejected" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.CREATED; // pending → 201
        };

        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * API 2 – Kiểm tra trạng thái duyệt
     * GET /api/v1/device/status?fingerprint=...
     * Public endpoint - không cần xác thực
     */
    @GetMapping("/device/status")
    public ResponseEntity<DeviceStatusResponse> checkDeviceStatus(@RequestParam String fingerprint) {
        if (fingerprint == null || fingerprint.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new DeviceStatusResponse("error", "Fingerprint không được để trống."));
        }

        DeviceStatusResponse response = deviceRequestService.checkDeviceStatus(fingerprint);

        if ("not_found".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * API 3 – Admin phê duyệt / từ chối thiết bị
     * PUT /api/v1/admin/device/update
     * Yêu cầu xác thực: chỉ ADMIN
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/device/update")
    public ResponseEntity<?> updateDeviceStatus(@RequestBody AdminDeviceUpdateRequest request) {
        if (request.getFingerprint() == null || request.getFingerprint().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Fingerprint không được để trống."));
        }

        if (request.getAction() == null || request.getAction().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Action không được để trống."));
        }

        DeviceStatusResponse result = deviceRequestService.updateDeviceStatus(
                request.getFingerprint(), request.getAction());

        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Không tìm thấy fingerprint này."));
        }

        if ("error".equals(result.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", result.getMessage()));
        }

        return ResponseEntity.ok(Map.of("success", true, "message", result.getMessage()));
    }

    /**
     * API 4 – Admin lấy danh sách device requests
     * GET /api/v1/admin/device/requests?status=pending&page=0&size=20
     * - status: "pending" | "approved" | "rejected" | null (lấy tất cả)
     * - Có phân trang
     * Yêu cầu xác thực: chỉ ADMIN
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/device/requests")
    public ResponseEntity<Page<DeviceRequest>> getDeviceRequests(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<DeviceRequest> result = deviceRequestService.getDeviceRequests(status, page, size);
        return ResponseEntity.ok(result);
    }
}
