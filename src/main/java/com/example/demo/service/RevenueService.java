package com.example.demo.service;

import com.example.demo.config.KeyPricingConfig;
import com.example.demo.dto.RevenueStatisticsDTO;
import com.example.demo.repository.SubscriptionKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RevenueService {

    private final SubscriptionKeyRepository keyRepository;
    private final KeyPricingConfig keyPricingConfig;

    // Danh sách loại key theo thứ tự hiển thị
    private static final List<String> KEY_TYPES = List.of("1MONTH", "3MONTHS", "6MONTHS", "1YEAR");

    /**
     * Lấy thống kê doanh thu theo khoảng thời gian.
     * Mặc định 30 ngày gần nhất nếu không truyền fromDate/toDate.
     */
    public RevenueStatisticsDTO getRevenueByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        if (fromDate == null) {
            fromDate = LocalDateTime.now().minusDays(30);
        }
        if (toDate == null) {
            toDate = LocalDateTime.now();
        }

        List<Object[]> results = keyRepository.countUsedKeysByTypeAndDateRange(fromDate, toDate);
        return buildRevenueDTO(fromDate, toDate, results);
    }

    /**
     * Lấy thống kê doanh thu theo 1 ngày cụ thể.
     */
    public RevenueStatisticsDTO getRevenueByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Object[]> results = keyRepository.countUsedKeysByTypeForDay(startOfDay, endOfDay);
        return buildRevenueDTO(startOfDay, endOfDay, results);
    }

    /**
     * Build DTO doanh thu từ kết quả query
     */
    private RevenueStatisticsDTO buildRevenueDTO(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            List<Object[]> queryResults) {

        // Parse kết quả query thành map
        Map<String, Long> usedCountMap = new HashMap<>();
        for (Object[] row : queryResults) {
            String keyType = (String) row[0];
            Long count = (Long) row[1];
            usedCountMap.put(keyType, count);
        }

        // Build chi tiết từng loại key
        List<RevenueStatisticsDTO.KeyTypeRevenue> revenueDetails = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalKeysUsed = 0;

        for (String keyType : KEY_TYPES) {
            long keysUsed = usedCountMap.getOrDefault(keyType, 0L);
            BigDecimal unitPrice = keyPricingConfig.getPrice(keyType);
            BigDecimal revenue = unitPrice.multiply(BigDecimal.valueOf(keysUsed));

            revenueDetails.add(RevenueStatisticsDTO.KeyTypeRevenue.builder()
                    .keyType(keyType)
                    .keysUsed(keysUsed)
                    .unitPrice(unitPrice)
                    .revenue(revenue)
                    .build());

            totalRevenue = totalRevenue.add(revenue);
            totalKeysUsed += keysUsed;
        }

        return RevenueStatisticsDTO.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .totalRevenue(totalRevenue)
                .totalKeysUsed(totalKeysUsed)
                .revenueByKeyType(revenueDetails)
                .build();
    }
}
