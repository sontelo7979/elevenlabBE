package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.RevenueStatisticsDTO;
import com.example.demo.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/revenue")
@RequiredArgsConstructor
public class RevenueController {

    private final RevenueService revenueService;

    /**
     * API 1: Doanh thu theo khoảng thời gian
     *
     * GET /api/revenue/dashboard?fromDate=2024-01-01T00:00:00&toDate=2024-12-31T23:59:59
     *
     * Mặc định 30 ngày gần nhất nếu không truyền fromDate/toDate.
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<RevenueStatisticsDTO>> getRevenueDashboard(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        try {
            RevenueStatisticsDTO stats = revenueService.getRevenueByDateRange(fromDate, toDate);
            return ResponseEntity.ok(new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy thống kê doanh thu thành công",
                    stats
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lấy thống kê doanh thu thất bại: " + e.getMessage(),
                    null
            ));
        }
    }

    /**
     * API 2: Doanh thu theo ngày cụ thể
     *
     * GET /api/revenue/dashboard/by-date?date=2024-06-15
     */
    @GetMapping("/dashboard/by-date")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<RevenueStatisticsDTO>> getRevenueByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            RevenueStatisticsDTO stats = revenueService.getRevenueByDate(date);
            return ResponseEntity.ok(new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy doanh thu theo ngày thành công",
                    stats
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lấy doanh thu theo ngày thất bại: " + e.getMessage(),
                    null
            ));
        }
    }
}
