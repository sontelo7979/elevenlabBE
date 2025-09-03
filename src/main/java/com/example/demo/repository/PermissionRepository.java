package com.example.demo.repository;

import com.example.demo.model.EPermission;
import com.example.demo.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(EPermission name);
    Optional<Permission> findByName(String name);
    boolean existsByName(EPermission name);
}