package com.example.demo.config;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cấu hình giá bán key theo loại.
 * Giá này RIÊNG BIỆT với giá trong PaymentService (dùng cho dịch vụ khác).
 *
 * - 1MONTH  = 500,000 VND
 * - 3MONTHS = 1,200,000 VND
 * - 6MONTHS = 2,000,000 VND
 * - 1YEAR   = 3,500,000 VND
 */
@Component
public class KeyPricingConfig {

    private static final Map<String, BigDecimal> KEY_PRICES = new LinkedHashMap<>();

    static {
        KEY_PRICES.put("1MONTH",  new BigDecimal("500000"));
        KEY_PRICES.put("3MONTHS", new BigDecimal("1200000"));
        KEY_PRICES.put("6MONTHS", new BigDecimal("2000000"));
        KEY_PRICES.put("1YEAR",   new BigDecimal("3500000"));
    }

    /**
     * Lấy giá của key theo loại. Trả về BigDecimal.ZERO nếu loại không hợp lệ.
     */
    public BigDecimal getPrice(String keyType) {
        return KEY_PRICES.getOrDefault(keyType, BigDecimal.ZERO);
    }

    /**
     * Lấy toàn bộ bảng giá
     */
    public Map<String, BigDecimal> getAllPrices() {
        return Map.copyOf(KEY_PRICES);
    }
}
