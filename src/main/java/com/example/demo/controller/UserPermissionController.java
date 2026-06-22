package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.service.UserPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/user-permissions")
@RequiredArgsConstructor
public class UserPermissionController {

    private final UserPermissionService userPermissionService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<UserPermission>> getUserPermissions(@PathVariable Long userId) {
        return ResponseEntity.ok(userPermissionService.getUserPermissions(userId));
    }

    @PostMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<UserPermission> addUserPermission(
            @PathVariable Long userId,
            @RequestParam EPermission permission,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(userPermissionService.addPermission(userId, permission, reason));
    }

    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<Void> removeUserPermission(
            @PathVariable Long userId,
            @RequestParam EPermission permission) {
        userPermissionService.removePermission(userId, permission);
        return ResponseEntity.ok().build();
    }
}