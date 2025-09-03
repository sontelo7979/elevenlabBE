package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.UserPermissionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserPermissionService {

    private final UserPermissionRepository userPermissionRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    // Lấy tất cả permissions của user
    public List<UserPermission> getUserPermissions(Long userId) {
        return userPermissionRepository.findByUserId(userId);
    }

    // Lấy permissions hiện tại có hiệu lực của user
    public List<UserPermission> getActivePermissions(Long userId) {
        return userPermissionRepository.findActivePermissionsByUserId(userId);
    }

    // Thêm permission cho user
    @Transactional
    public UserPermission addPermission(Long userId, EPermission permissionName, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionName));

        // Kiểm tra xem user đã có permission này chưa
        Optional<UserPermission> existingPermission = userPermissionRepository
                .findByUserIdAndPermissionName(userId, permissionName.name());

        if (existingPermission.isPresent()) {
            UserPermission userPermission = existingPermission.get();
            userPermission.setGranted(true);
            userPermission.setReason(reason);
            userPermission.setCreatedAt(LocalDateTime.now());
            return userPermissionRepository.save(userPermission);
        }

        // Tạo mới user permission
        UserPermission userPermission = UserPermission.builder()
                .user(user)
                .permission(permission)
                .granted(true)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();

        return userPermissionRepository.save(userPermission);
    }

    // Thêm permission có thời hạn
    @Transactional
    public UserPermission addPermissionWithExpiry(Long userId, EPermission permissionName,
                                                  LocalDateTime expiresAt, String reason) {
        UserPermission userPermission = addPermission(userId, permissionName, reason);
        userPermission.setExpiresAt(expiresAt);
        return userPermissionRepository.save(userPermission);
    }

    // Xóa permission của user
    @Transactional
    public void removePermission(Long userId, EPermission permissionName) {
        userPermissionRepository.findByUserIdAndPermissionName(userId, permissionName.name())
                .ifPresent(userPermissionRepository::delete);
    }

    // Vô hiệu hóa permission (thay vì xóa)
    @Transactional
    public void disablePermission(Long userId, EPermission permissionName) {
        userPermissionRepository.findByUserIdAndPermissionName(userId, permissionName.name())
                .ifPresent(userPermission -> {
                    userPermission.setGranted(false);
                    userPermissionRepository.save(userPermission);
                });
    }

    // Kiểm tra user có permission không
    public boolean hasPermission(Long userId, EPermission permissionName) {
        return userPermissionRepository.existsByUserIdAndPermissionNameAndGrantedTrue(
                userId, permissionName.name());
    }


}