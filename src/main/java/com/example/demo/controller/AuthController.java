package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.JwtResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            ApiResponse<JwtResponse> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Đăng nhập thành công",
                    jwtResponse
            );
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            // Lỗi sai username hoặc password
            ApiResponse<JwtResponse> response = new ApiResponse<>(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Sai tên đăng nhập hoặc mật khẩu",
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (DisabledException e) {
            // Tài khoản bị disabled
            ApiResponse<JwtResponse> response = new ApiResponse<>(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Tài khoản đã bị vô hiệu hóa",
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (LockedException e) {
            // Tài khoản bị khóa
            ApiResponse<JwtResponse> response = new ApiResponse<>(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Tài khoản đã bị khóa",
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            // Các lỗi khác
            ApiResponse<JwtResponse> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Đăng nhập thất bại: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<ApiResponse<String>> register(
            @RequestHeader("Authorization") String token,
            @RequestBody RegisterRequest registerRequest) {
        try {
            String result = authService.registerUser(token, registerRequest);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.CREATED.value(),
                    "Đăng ký thành công",
                    result
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Đăng ký thất bại: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}