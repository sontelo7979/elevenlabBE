package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ApplyKeyRequest;
import com.example.demo.model.SubscriptionKey;
import com.example.demo.repository.SubscriptionKeyRepository;
import com.example.demo.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final SubscriptionKeyRepository keyRepository;
    @PostMapping("/apply-key")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> applyKey(@RequestBody ApplyKeyRequest request) {
        try {
            subscriptionService.applySubscriptionKey(request.getUserId(), request.getKeyCode());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Key applied successfully",
                    "keyStatus", "USED" // Trả về trạng thái key
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "keyStatus", "INVALID" // Trả về trạng thái key
            ));
        }
    }
    @GetMapping("/check-key/{keyCode}")
    public ResponseEntity<?> checkKey(@PathVariable String keyCode) {
        Optional<SubscriptionKey> keyOpt = keyRepository.findByCode(keyCode);

        if (keyOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Key does not exist"
            ));
        }

        SubscriptionKey key = keyOpt.get();

        if (key.isUsed()) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Key has already been used"
            ));
        }

        if (!key.isActive()) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Key is not active"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "valid", true,
                "message", "Key is valid",
                "duration", key.getDuration().toDays() + " days",
                "description", key.getDescription()
        ));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> generateKey(
            @RequestHeader("Authorization") String token, // Thêm token
            @RequestParam String durationType,
            @RequestParam(defaultValue = "true") boolean isActive) {

        try {
            SubscriptionKey key = subscriptionService.generateKey(token,durationType, isActive);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "key", key,
                    "message", "Key generated successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * API 1: Lấy danh sách key (có paging, search và filter theo type)
     * Cách dùng:
     * GET /api/subscription/keys/my-keys?search=ABC&keyType=1MONTH&page=0&size=10
     */
    @GetMapping("/my-keys")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<SubscriptionKey>>> getMyKeys(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String keyType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<SubscriptionKey> keys = subscriptionService.getMyKeys(token, search, keyType, page, size);

            ApiResponse<Page<SubscriptionKey>> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy danh sách key thành công",
                    keys
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Page<SubscriptionKey>> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lấy danh sách thất bại: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API 2: Dashboard thống kê số key theo khoảng thời gian
     * Cách dùng:
     * GET /api/subscription/keys/statistics?fromDate=2024-01-01T00:00:00&toDate=2024-12-31T23:59:59
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getKeyStatistics(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        try {
            Map<String, Object> stats = subscriptionService.getKeyStatistics(token, fromDate, toDate);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy thống kê key thành công",
                    stats
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lấy thống kê thất bại: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}