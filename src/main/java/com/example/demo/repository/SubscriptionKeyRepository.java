package com.example.demo.repository;

import com.example.demo.model.SubscriptionKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionKeyRepository extends JpaRepository<SubscriptionKey, Long> {
    Optional<SubscriptionKey> findByCode(String code);
    boolean existsByCodeAndIsUsedFalse(String code);
}