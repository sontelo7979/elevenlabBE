package com.example.demo.repository;

import com.example.demo.model.SubscriptionKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionKeyRepository extends JpaRepository<SubscriptionKey, Long> {
    Optional<SubscriptionKey> findByCode(String code);
    boolean existsByCodeAndIsUsedFalse(String code);

    // 1. Hàm get list key theo created_by_user_id (có paging, search và filter theo type)
    @Query("SELECT k FROM SubscriptionKey k WHERE " +
            "k.createdByUserId = :userId " +
            "AND (:search IS NULL OR :search = '' OR k.code LIKE %:search% OR k.description LIKE %:search%) " +
            "AND (:keyType IS NULL OR :keyType = '' OR k.keyType = :keyType)")
    Page<SubscriptionKey> findByCreatedByUserIdWithFilters(
            @Param("userId") Long userId,
            @Param("search") String search,
            @Param("keyType") String keyType,
            Pageable pageable);

    // 2. Hàm thống kê số key theo khoảng thời gian và phân loại
    @Query("SELECT k.keyType as keyType, COUNT(k) as count " +
            "FROM SubscriptionKey k " +
            "WHERE k.createdByUserId = :userId " +
            "AND k.createdAt BETWEEN :fromDate AND :toDate " +
            "GROUP BY k.keyType")
    List<Object[]> countKeysByTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
}