package com.example.demo.dto;

import com.example.demo.model.UserPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionDTO {
    private Long id;
    private String permissionName;       // chỉ lấy tên, không lồng cả entity Permission
    private String permissionDescription;
    private Boolean granted;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String reason;

    public static UserPermissionDTO fromEntity(UserPermission entity) {
        return UserPermissionDTO.builder()
                .id(entity.getId())
                .permissionName(entity.getPermission().getName().name())
                .permissionDescription(entity.getPermission().getDescription())
                .granted(entity.getGranted())
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .reason(entity.getReason())
                .build();
    }
}