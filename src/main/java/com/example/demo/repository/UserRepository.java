package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByRegisteredDeviceIdAndIdNot(String deviceId, Long userId);

    // 1. Cho ADMIN - lấy tất cả user (có filter)
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN u.registeredByCollaborator rc " +
            "LEFT JOIN rc.user cu " +
            "WHERE (:search IS NULL OR :search = '' " +
            "   OR u.username LIKE %:search% " +
            "   OR u.email LIKE %:search%) " +
            "AND (:isActive IS NULL OR u.isActive = :isActive) " +
            "AND (:role IS NULL OR EXISTS (SELECT r FROM u.roles r WHERE r.name = :role))")
    Page<User> findUsersForAdmin(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("role") String role,
            Pageable pageable);

    // 2. Cho CTV - chỉ lấy user do họ đăng ký (có filter)
    @Query("SELECT u FROM User u " +
            "WHERE u.registeredByCollaborator.user.id = :ctvUserId " +
            "AND (:search IS NULL OR :search = '' " +
            "   OR u.username LIKE %:search% " +
            "   OR u.email LIKE %:search%) " +
            "AND (:isActive IS NULL OR u.isActive = :isActive)")
    Page<User> findUsersForCTV(
            @Param("ctvUserId") Long ctvUserId,
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
}