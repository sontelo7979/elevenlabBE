package com.example.demo.repository;

import com.example.demo.model.UserSubscriptionKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSubscriptionKeyRepository extends JpaRepository<UserSubscriptionKey, Long> {
    // Lấy lịch sử theo user
    List<UserSubscriptionKey> findByUserId(Long userId);
    Page<UserSubscriptionKey> findByUserId(Long userId, Pageable pageable);

    // Lấy lịch sử theo collaborator
    List<UserSubscriptionKey> findByCollaboratorUserId(Long collaboratorUserId);
    Page<UserSubscriptionKey> findByCollaboratorUserId(Long collaboratorUserId, Pageable pageable);

    // Đếm số lần gia hạn của user
    long countByUserId(Long userId);

    @Query("SELECT u FROM UserSubscriptionKey u WHERE u.user.id = :userId ORDER BY u.appliedAt DESC")
    List<UserSubscriptionKey> findLatestByUserId(@Param("userId") Long userId);

    @Query("SELECT u FROM UserSubscriptionKey u WHERE u.user.id = :userId ORDER BY u.appliedAt DESC")
    Page<UserSubscriptionKey> findLatestByUserId(@Param("userId") Long userId, Pageable pageable);
}
