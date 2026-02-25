package com.example.demo.service;

import com.example.demo.model.Collaborator;
import com.example.demo.model.SubscriptionKey;
import com.example.demo.model.User;
import com.example.demo.model.UserSubscriptionKey;
import com.example.demo.repository.CollaboratorRepository;
import com.example.demo.repository.SubscriptionKeyRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserSubscriptionKeyRepository;
import com.example.demo.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final UserRepository userRepository;
    private final SubscriptionKeyRepository keyRepository;
    private final UserSubscriptionKeyRepository userKeyRepository;
    private final CollaboratorRepository collaboratorRepository;

    private final JwtUtils jwtUtils;
    @Transactional
    public void applySubscriptionKey(Long userId, String token, String keyCode) {
        // Lấy collaboratorUserId từ token
        String jwt = token.substring(7);
        Long collaboratorUserId = jwtUtils.getUserIdFromToken(jwt);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SubscriptionKey key = keyRepository.findByCode(keyCode)
                .orElseThrow(() -> new RuntimeException("Invalid subscription key"));

        if (!key.isActive()) {
            throw new RuntimeException("Key is not active");
        }
        if (key.isUsed()) {
            throw new RuntimeException("Key has been used");
        }

        LocalDateTime currentEndDate = user.getEndDate() != null ?
                user.getEndDate() : LocalDateTime.now();

        if (currentEndDate.isBefore(LocalDateTime.now())) {
            currentEndDate = LocalDateTime.now();
        }

        LocalDateTime newEndDate = currentEndDate.plus(key.getDuration());

        // Lưu lịch sử sử dụng key
        UserSubscriptionKey userKey = new UserSubscriptionKey();
        userKey.setUser(user);
        userKey.setSubscriptionKey(key);
        userKey.setNewEndDate(newEndDate);
        userKey.setAppliedAt(LocalDateTime.now());
        userKey.setCollaboratorUserId(collaboratorUserId); // Lưu collaboratorUserId
        userKeyRepository.save(userKey);

        user.setEndDate(newEndDate);
        userRepository.save(user);

        key.setUsed(true);
        keyRepository.save(key);
    }
    @Transactional
    public SubscriptionKey generateKey(String token, String durationType, boolean isActive) {

        // Lấy userId từ token
        String jwt = token.substring(7);
        Long userId = jwtUtils.getUserIdFromToken(jwt);

        // Tạo mã key ngẫu nhiên
        String keyCode = generateRandomKeyCode();

        // Xác định duration dựa trên loại
        Duration duration;
        String description;
        String keyType = durationType.toUpperCase(); // Lưu trực tiếp string: "1MONTH", "3MONTHS",...

        switch (keyType) {
            case "1MONTH":
                duration = Duration.ofDays(30);
                description = "Gia hạn 1 tháng";
                break;
            case "3MONTHS":
                duration = Duration.ofDays(90);
                description = "Gia hạn 3 tháng";
                break;
            case "6MONTHS":
                duration = Duration.ofDays(180);
                description = "Gia hạn 6 tháng";
                break;
            case "1YEAR":
                duration = Duration.ofDays(365);
                description = "Gia hạn 1 năm";
                break;
            default:
                throw new IllegalArgumentException("Invalid duration type");
        }

        // Tạo và lưu key
        SubscriptionKey key = SubscriptionKey.builder()
                .code(keyCode)
                .description(description)
                .duration(duration)
                .keyType(keyType) // Lưu string
                .active(isActive)
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .createdByUserId(userId)
                .build();

        return keyRepository.save(key);
    }
    /**
     * 1. Lấy danh sách SubscriptionKey do user hiện tại tạo (có paging, search và filter theo type)
     */
    public Page<SubscriptionKey> getMyKeys(
            String token,
            String search,
            String keyType,
            int page,
            int size) {
        String jwt = token.substring(7);
        Long userId = jwtUtils.getUserIdFromToken(jwt);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return keyRepository.findByCreatedByUserIdWithFilters(userId, search, keyType, pageable);
    }

    /**
     * 2. Dashboard thống kê số key theo khoảng thời gian và phân loại
     */
    public Map<String, Object> getKeyStatistics(
            String token,
            LocalDateTime fromDate,
            LocalDateTime toDate) {
        String jwt = token.substring(7);
        Long userId = jwtUtils.getUserIdFromToken(jwt);

        // Mặc định lấy 30 ngày gần nhất nếu không có fromDate, toDate
        if (fromDate == null) {
            fromDate = LocalDateTime.now().minusDays(30);
        }
        if (toDate == null) {
            toDate = LocalDateTime.now();
        }

        // Tìm collaborator ID từ user ID
        Collaborator collaborator = collaboratorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Collaborator not found for user: " + userId));
        Long collaboratorId = collaborator.getId();
        Double commissionRate = collaborator.getCommissionRate();


        // Lấy thống kê keys từ repository
        List<Object[]> results = keyRepository.countKeysByTypeAndDateRange(userId, fromDate, toDate);

        // Map kết quả keys
        Map<String, Long> stats = new HashMap<>();
        stats.put("1MONTH", 0L);
        stats.put("3MONTHS", 0L);
        stats.put("6MONTHS", 0L);
        stats.put("1YEAR", 0L);

        for (Object[] result : results) {
            String keyType = (String) result[0];
            Long count = (Long) result[1];
            stats.put(keyType, count);
        }

        // Tổng số key
        long totalKeys = stats.values().stream().mapToLong(Long::longValue).sum();

        // Đếm số users được đăng ký bởi collaborator này
        long totalUsers = userRepository.countByRegisteredByCollaboratorId(collaboratorId);

        Map<String, Object> response = new HashMap<>();
        response.put("fromDate", fromDate);
        response.put("toDate", toDate);
        response.put("totalKeys", totalKeys);
        response.put("totalUsers", totalUsers);
        response.put("commissionRate", commissionRate); // Thêm commission rate
        response.put("statistics", stats);

        return response;
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