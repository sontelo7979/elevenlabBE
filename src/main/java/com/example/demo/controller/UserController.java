package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PageResponse;
import com.example.demo.dto.UserDTO;
import com.example.demo.security.JwtUtils;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    /**
     * API DUY NHẤT để lấy danh sách user
     * - ADMIN: xem tất cả user
     * - STAFF (CTV): chỉ xem user do mình đăng ký
     *
     * @param search Tìm kiếm theo username hoặc email (để trống nếu không search)
     * @param isActive Lọc theo trạng thái active (để trống nếu không lọc)
     * @param role Lọc theo role (chỉ dành cho ADMIN)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getUsers(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        try {
            PageResponse<UserDTO> users = userService.getUsers(
                    token, search, isActive, role, page, size, sortBy, sortDirection);

            ApiResponse<PageResponse<UserDTO>> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy danh sách user thành công",
                    users
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<PageResponse<UserDTO>> response = new ApiResponse<>(
                    HttpStatus.FORBIDDEN.value(),
                    "Lấy danh sách thất bại: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    /**
     * API lấy chi tiết user theo ID
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {
        try {
            UserDTO user = userService.getUserDetail(userId, token);
            ApiResponse<UserDTO> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy thông tin user thành công",
                    user
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<UserDTO> response = new ApiResponse<>(
                    HttpStatus.FORBIDDEN.value(),
                    "Lấy thông tin thất bại: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    /**
     * API lấy thông tin user hiện tại
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            Long currentUserId = jwtUtils.getUserIdFromToken(jwt);

            UserDTO user = userService.getUserDetail(currentUserId, token);

            ApiResponse<UserDTO> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy thông tin user hiện tại thành công",
                    user
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<UserDTO> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lấy thông tin thất bại: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}