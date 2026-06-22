package com.example.demo.controller;

import com.example.demo.dto.UserPermissionDTO;
import com.example.demo.model.EPermission;
import com.example.demo.model.UserPermission;
import com.example.demo.service.UserPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/user-permissions")
@RequiredArgsConstructor
public class UserPermissionController {

    private final UserPermissionService userPermissionService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<UserPermissionDTO>> getUserPermissions(@PathVariable Long userId) {
        List<UserPermissionDTO> result = userPermissionService.getUserPermissions(userId)
                .stream()
                .map(UserPermissionDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<UserPermissionDTO> addUserPermission(
            @PathVariable Long userId,
            @RequestParam EPermission permission,
            @RequestParam(required = false) String reason) {
        UserPermission saved = userPermissionService.addPermission(userId, permission, reason);
        return ResponseEntity.ok(UserPermissionDTO.fromEntity(saved));
    }

    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Void> removeUserPermission(
            @PathVariable Long userId,
            @RequestParam EPermission permission) {
        userPermissionService.removePermission(userId, permission);
        return ResponseEntity.ok().build();
    }
}