package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String username;

    @Column(unique = true, length = 50)
    private String email;

    @Column(length = 120)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Column(name = "start_date")
    private LocalDateTime startDate; // Thêm giờ, phút, giây

    @Column(name = "end_date")
    private LocalDateTime endDate; // Thêm giờ, phút, giây

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "registered_device_id")
    private String registeredDeviceId;

    @Column(name = "device_registered_at")
    private LocalDateTime deviceRegisteredAt;

    // Thêm trường để biết CTV nào đã đăng ký tài khoản này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by_collaborator_id")
    private Collaborator registeredByCollaborator;

    // Phương thức kiểm tra tài khoản còn hiệu lực
    public boolean isAccountValid() {
        // Nếu không phải role CUSTOMER thì luôn valid (ADMIN, STAFF)
        if (this.roles.stream().noneMatch(role -> role.getName() == ERole.ROLE_CUSTOMER)) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();

        // Nếu tài khoản không active -> invalid
        if (!isActive) {
            return false;
        }

        // Xử lý trường hợp startDate và endDate bị null
        if (startDate == null && endDate == null) {
            // Nếu cả 2 đều null -> coi như tài khoản vĩnh viễn (valid)
            return true;
        }

        if (startDate == null) {
            // Chỉ có endDate -> kiểm tra không quá endDate
            return !now.isAfter(endDate);
        }

        if (endDate == null) {
            // Chỉ có startDate -> kiểm tra đã qua startDate chưa
            return !now.isBefore(startDate);
        }

        // Có cả startDate và endDate -> kiểm tra trong khoảng
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserSubscriptionKey> subscriptionKeys = new HashSet<>();

    // Thêm quan hệ với UserPermission
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserPermission> userPermissions = new HashSet<>();

}