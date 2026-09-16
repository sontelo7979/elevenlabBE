package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatisticsDTO {

    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    // Tổng doanh thu toàn bộ
    private BigDecimal totalRevenue;

    // Tổng số key đã sử dụng
    private long totalKeysUsed;

    // Chi tiết doanh thu theo từng loại key
    private List<KeyTypeRevenue> revenueByKeyType;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyTypeRevenue {
        private String keyType;        // "1MONTH", "3MONTHS", "6MONTHS", "1YEAR"
        private long keysUsed;         // Số key đã sử dụng
        private BigDecimal unitPrice;  // Giá mỗi key
        private BigDecimal revenue;    // keysUsed * unitPrice
    }
}
