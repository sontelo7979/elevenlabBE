package com.example.demo.controller;

import com.example.demo.dto.ApplyKeyRequest;
import com.example.demo.model.SubscriptionKey;
import com.example.demo.repository.SubscriptionKeyRepository;
import com.example.demo.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateKey(
            @RequestParam String durationType,
            @RequestParam(defaultValue = "true") boolean isActive) {

        try {
            SubscriptionKey key = subscriptionService.generateKey(durationType, isActive);
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
}