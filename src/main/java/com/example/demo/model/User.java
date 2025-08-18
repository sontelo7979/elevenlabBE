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

    // Phương thức kiểm tra tài khoản còn hiệu lực
    public boolean isAccountValid() {
        if (!this.roles.stream().anyMatch(role -> role.getName() == ERole.ROLE_CUSTOMER)) {
            return true;
        }
        LocalDateTime now = LocalDateTime.now();
        return isActive && !now.isBefore(startDate) && !now.isAfter(endDate);
    }
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserSubscriptionKey> subscriptionKeys = new HashSet<>();
}