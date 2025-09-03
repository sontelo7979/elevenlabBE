package com.example.demo.repository;

import com.example.demo.model.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    List<UserPermission> findByUserId(Long userId);

    @Query("SELECT up FROM UserPermission up WHERE up.user.id = :userId AND up.permission.name = :permissionName")
    Optional<UserPermission> findByUserIdAndPermissionName(@Param("userId") Long userId,
                                                           @Param("permissionName") String permissionName);

    @Query("SELECT CASE WHEN COUNT(up) > 0 THEN true ELSE false END " +
            "FROM UserPermission up WHERE up.user.id = :userId AND up.permission.name = :permissionName AND up.granted = true")
    boolean existsByUserIdAndPermissionNameAndGrantedTrue(@Param("userId") Long userId,
                                                          @Param("permissionName") String permissionName);

    @Query("SELECT up FROM UserPermission up WHERE up.user.id = :userId AND up.granted = true " +
            "AND (up.expiresAt IS NULL OR up.expiresAt > CURRENT_TIMESTAMP)")
    List<UserPermission> findActivePermissionsByUserId(@Param("userId") Long userId);
}