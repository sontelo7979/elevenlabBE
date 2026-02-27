package com.example.demo.service;

import com.example.demo.dto.PageResponse;
import com.example.demo.dto.UserDTO;
import com.example.demo.model.*;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserPermissionRepository;
import com.example.demo.repository.UserSubscriptionKeyRepository;
import com.example.demo.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final UserSubscriptionKeyRepository userSubscriptionKeyRepository;
    private final JwtUtils jwtUtils;

    // Một method duy nhất cho cả ADMIN và CTV
    public PageResponse<UserDTO> getUsers(
            String token,
            String search,
            Boolean isActive,
            String role,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        String jwt = token.substring(7);
        Long currentUserId = jwtUtils.getUserIdFromToken(jwt);
        List<String> roles = jwtUtils.getRolesFromToken(jwt);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage;

        // ADMIN
        if (roles.contains("ROLE_ADMIN")) {
            userPage = userRepository.findUsersForAdmin(search, isActive, role, pageable);
            return PageResponse.fromPage(userPage.map(this::convertToDTOForAdmin));
        }

        // STAFF (CTV)
        if (roles.contains("ROLE_STAFF")) {
            userPage = userRepository.findUsersForCTV(currentUserId, search, isActive, pageable);
            return PageResponse.fromPage(userPage.map(this::convertToDTOForCTV));
        }

        throw new RuntimeException("Bạn không có quyền xem danh sách user");
    }

    // Lấy chi tiết user
    public UserDTO getUserDetail(Long userId, String token) {
        String jwt = token.substring(7);
        Long currentUserId = jwtUtils.getUserIdFromToken(jwt);
        List<String> roles = jwtUtils.getRolesFromToken(jwt);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        // ADMIN
        if (roles.contains("ROLE_ADMIN")) {
            return convertToDTOForAdmin(user);
        }

        // STAFF
        if (roles.contains("ROLE_STAFF")) {
            if (user.getRegisteredByCollaborator() == null ||
                    !user.getRegisteredByCollaborator().getUser().getId().equals(currentUserId)) {
                throw new RuntimeException("Bạn không có quyền xem thông tin user này");
            }
            return convertToDTOForCTV(user);
        }

        throw new RuntimeException("Bạn không có quyền xem thông tin user");
    }

    // Convert methods
    private UserDTO convertToDTOForAdmin(User user) {
        try {
            List<String> permissions = userPermissionRepository.findActivePermissionsByUserId(user.getId())
                    .stream()
                    .map(up -> up.getPermission().getName().getCode())
                    .collect(Collectors.toList());

            return UserDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .startDate(user.getStartDate())
                    .endDate(user.getEndDate())
                    .isActive(user.getIsActive())
                    .isAccountValid(user.isAccountValid())
                    .registeredDeviceId(user.getRegisteredDeviceId())
                    .deviceRegisteredAt(user.getDeviceRegisteredAt())
                    .roles(user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                    .permissions(permissions)
                    .registeredByCollaboratorId(
                            user.getRegisteredByCollaborator() != null ?
                                    user.getRegisteredByCollaborator().getId() : null)
                    .registeredByCollaboratorUsername(
                            user.getRegisteredByCollaborator() != null &&
                                    user.getRegisteredByCollaborator().getUser() != null ?
                                    user.getRegisteredByCollaborator().getUser().getUsername() : null)
                    .build();
        } catch (Exception e) {
            System.err.println("Lỗi khi convert user ID: " + user.getId());
            System.err.println("Username: " + user.getUsername());
            System.err.println("StartDate: " + user.getStartDate());
            System.err.println("EndDate: " + user.getEndDate());
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private UserDTO convertToDTOForCTV(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .startDate(user.getStartDate())
                .endDate(user.getEndDate())
                .isActive(user.getIsActive())
                .isAccountValid(user.isAccountValid())
                .registeredDeviceId(user.getRegisteredDeviceId())
                .deviceRegisteredAt(user.getDeviceRegisteredAt())
                .build();
    }
}