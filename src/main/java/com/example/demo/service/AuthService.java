package com.example.demo.service;

import com.example.demo.dto.JwtResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.ERole;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtils;
import com.example.demo.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final DeviceRegistrationService deviceRegistrationService;
    private final HttpServletRequest httpServletRequest;

    public AuthService(AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
                       DeviceRegistrationService deviceRegistrationService,
                       HttpServletRequest httpServletRequest) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.deviceRegistrationService = deviceRegistrationService;
        this.httpServletRequest = httpServletRequest;
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        // Lấy thông tin user
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        // Chỉ kiểm tra thiết bị đăng ký cho ROLE_CUSTOMER
        if (roles.contains("ROLE_CUSTOMER")) {
            // Lấy device ID từ client
            String deviceId = getClientDeviceId();

            // Kiểm tra thiết bị đã đăng ký
            if (!deviceRegistrationService.isDeviceRegisteredForUser(userDetails.getId(), deviceId)) {
                // Nếu user chưa có thiết bị đăng ký, thực hiện đăng ký
                if (userDetails.getRegisteredDeviceId() == null) {
                    deviceRegistrationService.registerDeviceForUser(userDetails.getId(), deviceId);
                } else {
                    throw new RuntimeException("Tài khoản này đã được đăng ký với thiết bị khác. " +
                            "Vui lòng sử dụng thiết bị đã đăng ký hoặc liên hệ quản trị viên.");
                }
            }
        }
        return JwtResponse.builder()
                .token(jwt)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .roles(roles)
                .startDate(userDetails.getStartDate())
                .endDate(userDetails.getEndDate())
                .isActive(userDetails.getIsActive())
                .registeredDeviceId(userDetails.getRegisteredDeviceId())
                .deviceRegisteredAt(userDetails.getDeviceRegisteredAt())
                .build();
    }
    // Lấy device ID từ client
    private String getClientDeviceId() {
        String deviceId = httpServletRequest.getHeader("X-Device-Id");
        if (deviceId == null || deviceId.isEmpty()) {
            throw new RuntimeException("Thiết bị không được xác định. Vui lòng cung cấp Device ID.");
        }
        return deviceId;
    }
    public String registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Create new user's account
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .startDate(registerRequest.getStartDate())
                .endDate(registerRequest.getEndDate())
                .isActive(true)
                .build();

        Set<Integer> intRoles = registerRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (intRoles == null) {
            // Default to staff (2)
            Role staffRole = roleRepository.findByName(ERole.ROLE_STAFF)
                    .orElseThrow(() -> new RuntimeException("Error: Role not found"));
            roles.add(staffRole);
        } else {
            intRoles.forEach(role -> {
                switch (role) {
                    case 1: // Admin
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                        roles.add(adminRole);
                        break;
                    case 2: // Staff
                        Role staffRole = roleRepository.findByName(ERole.ROLE_STAFF)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                        roles.add(staffRole);
                        break;
                    case 3: // Customer
                        Role customerRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                        roles.add(customerRole);
                        break;
                    default: // Default to staff if invalid value
                        Role defaultRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                        roles.add(defaultRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);
        return "User registered successfully!";
    }
}
//