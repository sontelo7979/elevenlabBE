package com.example.demo.service;

import com.example.demo.dto.JwtResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.*;
import com.example.demo.repository.*;
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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final UserSubscriptionKeyRepository userSubscriptionKeyRepository; // Thêm
    private final CollaboratorRepository collaboratorRepository; // Thêm
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final DeviceRegistrationService deviceRegistrationService;
    private final HttpServletRequest httpServletRequest;
    private final UserPermissionService userPermissionService; // Thêm service mới



    public AuthService(AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
                       PermissionRepository permissionRepository,
                       UserPermissionRepository userPermissionRepository,
                       UserSubscriptionKeyRepository userSubscriptionKeyRepository, // Thêm
                       CollaboratorRepository collaboratorRepository, // Thêm
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
                       DeviceRegistrationService deviceRegistrationService,
                       HttpServletRequest httpServletRequest,
                       UserPermissionService userPermissionService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.userSubscriptionKeyRepository = userSubscriptionKeyRepository; // Thêm
        this.collaboratorRepository = collaboratorRepository; // Thêm
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.deviceRegistrationService = deviceRegistrationService;
        this.httpServletRequest = httpServletRequest;
        this.userPermissionService = userPermissionService;
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
        List<String> permissions = userPermissionService.getActivePermissions(userDetails.getId()).stream()
                .map(up -> up.getPermission().getName().getCode())
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
                .permissions(permissions) // Thêm permissions vào response
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
    public String registerUser(String token, RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Lấy userId từ token (người đang thực hiện register)
        String jwt = token.substring(7);
        Long currentUserId = jwtUtils.getUserIdFromToken(jwt);
        List<String> rolesFromToken = jwtUtils.getRolesFromToken(jwt);

        // Xác định role của người đang đăng ký
        boolean isAdmin = rolesFromToken.contains("ROLE_ADMIN");
        boolean isCTV = rolesFromToken.contains("ROLE_STAFF");

        // Tìm collaborator nếu current user là CTV
        Collaborator collaborator = null;
        if (isCTV) {
            collaborator = collaboratorRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin cộng tác viên"));

            if (!collaborator.getIsActive()) {
                throw new RuntimeException("Cộng tác viên đã bị khóa");
            }
        }

        // Create new user's account
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .startDate(registerRequest.getStartDate())
                .endDate(registerRequest.getEndDate())
                .isActive(true)
                .registeredByCollaborator(collaborator)
                .build();

        Set<Integer> intRoles = registerRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (intRoles == null || intRoles.isEmpty()) {
            // Default to customer nếu không gửi roles
            Role customerRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Error: Role not found"));
            roles.add(customerRole);
        } else {
            // Kiểm tra quyền dựa trên role của người đăng ký
            for (Integer role : intRoles) {
                switch (role) {
                    case 2: // Staff
                        // Chỉ ADMIN mới được tạo STAFF
                        if (!isAdmin) {
                            throw new RuntimeException("Bạn không có quyền tạo tài khoản STAFF. Chỉ ADMIN mới được phép.");
                        }
                        Role staffRole = roleRepository.findByName(ERole.ROLE_STAFF)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                        roles.add(staffRole);
                        break;

                    case 3: // Customer
                        // Cả ADMIN và CTV đều được tạo CUSTOMER
                        Role customerRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                        roles.add(customerRole);
                        break;

                    case 1: // Admin
                        // Không ai được tạo ADMIN qua API này
                        throw new RuntimeException("Không thể tạo tài khoản ADMIN qua API này");

                    default:
                        throw new RuntimeException("Role không hợp lệ: " + role + ". Chỉ chấp nhận 2 (STAFF) hoặc 3 (CUSTOMER)");
                }
            }
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        if (registerRequest.getPermissions() != null && !registerRequest.getPermissions().isEmpty()) {
            assignPermissionsToUser(savedUser, registerRequest.getPermissions());
        }

        return "User registered successfully!";
    }

    // Thêm method lưu lịch sử

    private void assignPermissionsToUser(User user, Set<String> permissionCodes) {
        permissionCodes.forEach(permissionCode -> {
            // Chuyển đổi từ String sang EPermission
            EPermission ePermission;
            try {
                ePermission = EPermission.valueOf(permissionCode);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Error: Invalid permission code: " + permissionCode);
            }

            Permission permission = permissionRepository.findByName(ePermission)
                    .orElseThrow(() -> new RuntimeException("Error: Permission not found: " + permissionCode));

            UserPermission userPermission = UserPermission.builder()
                    .user(user)
                    .permission(permission)
                    .granted(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            userPermissionRepository.save(userPermission);
        });
    }
}
//