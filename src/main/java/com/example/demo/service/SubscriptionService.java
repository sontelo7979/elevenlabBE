package com.example.demo.service;

import com.example.demo.model.SubscriptionKey;
import com.example.demo.model.User;
import com.example.demo.model.UserSubscriptionKey;
import com.example.demo.repository.SubscriptionKeyRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserSubscriptionKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final UserRepository userRepository;
    private final SubscriptionKeyRepository keyRepository;
    private final UserSubscriptionKeyRepository userKeyRepository;

    @Transactional
    public void applySubscriptionKey(Long userId, String keyCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SubscriptionKey key = keyRepository.findByCode(keyCode)
                .orElseThrow(() -> new RuntimeException("Invalid subscription key"));

        if (!key.isActive()) {
            throw new RuntimeException("Key is not active");
        }
        if (!keyRepository.existsByCodeAndIsUsedFalse(keyCode)) {
            throw new RuntimeException("Key has been used or does not exist");
        }
        LocalDateTime currentEndDate = user.getEndDate() != null ?
                user.getEndDate() : LocalDateTime.now();

        // Nếu tài khoản đã hết hạn, tính từ thời điểm hiện tại
        if (currentEndDate.isBefore(LocalDateTime.now())) {
            currentEndDate = LocalDateTime.now();
        }

        LocalDateTime newEndDate = currentEndDate.plus(key.getDuration());

        // Lưu lịch sử sử dụng key
        UserSubscriptionKey userKey = new UserSubscriptionKey();
        userKey.setUser(user);
        userKey.setSubscriptionKey(key);
        userKey.setNewEndDate(newEndDate);
        userKeyRepository.save(userKey);

        // Cập nhật ngày hết hạn mới cho user
        user.setEndDate(newEndDate);
        userRepository.save(user);

        // Đánh dấu key đã sử dụng
        key.setUsed(true);
        keyRepository.save(key);
    }

    @Transactional
    public SubscriptionKey generateKey(String durationType, boolean isActive) {
        // Tạo mã key ngẫu nhiên (ví dụ: "ABC123-XYZ456")
        String keyCode = generateRandomKeyCode();

        // Xác định duration dựa trên loại
        Duration duration = switch (durationType.toUpperCase()) {
            case "1MONTH" -> Duration.ofDays(30);
            case "3MONTHS" -> Duration.ofDays(90);
            case "6MONTHS" -> Duration.ofDays(180);
            case "1YEAR" -> Duration.ofDays(365);
            default -> throw new IllegalArgumentException("Invalid duration type");
        };

        // Tạo description tự động
        String description = "Gia hạn " + durationType.toLowerCase()
                .replace("1month", "1 tháng")
                .replace("3months", "3 tháng")
                .replace("1year", "1 năm");

        // Tạo và lưu key
        SubscriptionKey key = SubscriptionKey.builder()
                .code(keyCode)
                .description(description)
                .duration(duration)
                .active(isActive)
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();

        return keyRepository.save(key);
    }

    private String generateRandomKeyCode() {
        // Tạo key dạng: ABCDE-12345-FGHIJ
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        sb.append("-");
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        sb.append("-");
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }
}