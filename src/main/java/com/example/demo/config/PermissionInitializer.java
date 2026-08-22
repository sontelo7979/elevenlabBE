package com.example.demo.config;

import com.example.demo.model.EPermission;
import com.example.demo.model.Permission;
import com.example.demo.repository.PermissionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@Order(2)
public class PermissionInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;

    public PermissionInitializer(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Arrays.stream(EPermission.values()).forEach(ep -> {
            if (permissionRepository.findByName(ep).isEmpty()) {
                Permission permission = Permission.builder()
                        .name(ep)
                        .description(ep.getCode())
                        .createdAt(LocalDateTime.now())
                        .build();
                permissionRepository.save(permission);
            }
        });
    }
}