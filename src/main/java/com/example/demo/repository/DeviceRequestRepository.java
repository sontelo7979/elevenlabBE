package com.example.demo.repository;

import com.example.demo.model.DeviceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRequestRepository extends JpaRepository<DeviceRequest, Long> {

    Optional<DeviceRequest> findByFingerprint(String fingerprint);

    boolean existsByFingerprint(String fingerprint);
}
