package com.example.demo.config;

import com.example.demo.model.ERole;
import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Arrays.asList(ERole.ROLE_ADMIN, ERole.ROLE_STAFF, ERole.ROLE_CUSTOMER).forEach(role -> {
            if (!roleRepository.existsByName(role)) {
                Role newRole = Role.builder()
                        .name(role)
                        .build();
                roleRepository.save(newRole);
            }
        });
    }
}