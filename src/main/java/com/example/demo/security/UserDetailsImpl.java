package com.example.demo.security;

import com.example.demo.model.User;
import jakarta.persistence.Column;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;
    private String password;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private String registeredDeviceId;
    private LocalDateTime deviceRegisteredAt;
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String email, String password,
                           LocalDateTime startDate, LocalDateTime endDate, Boolean isActive,
                           String registeredDeviceId, LocalDateTime deviceRegisteredAt,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.authorities = authorities;
        this.registeredDeviceId = registeredDeviceId;
        this.deviceRegisteredAt = deviceRegisteredAt;
    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getStartDate(),
                user.getEndDate(),
                user.getIsActive(),
                user.getRegisteredDeviceId(),
                user.getDeviceRegisteredAt(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public String getRegisteredDeviceId() {
        return registeredDeviceId;
    }

    public LocalDateTime getDeviceRegisteredAt() {
        return deviceRegisteredAt;
    }


}