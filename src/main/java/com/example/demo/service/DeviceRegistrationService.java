// DeviceRegistrationService.java
package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DeviceRegistrationService {
    private final UserRepository userRepository;

    public DeviceRegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean registerDeviceForUser(Long userId, String deviceId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        // Kiểm tra nếu deviceId đã được đăng ký cho user khác
        if (userRepository.existsByRegisteredDeviceIdAndIdNot(deviceId, userId)) {
            throw new RuntimeException("Thiết bị này đã được đăng ký cho tài khoản khác");
        }

        user.setRegisteredDeviceId(deviceId);
        user.setDeviceRegisteredAt(LocalDateTime.now());
        userRepository.save(user);

        return true;
    }

    public boolean isDeviceRegisteredForUser(Long userId, String deviceId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        return deviceId.equals(user.getRegisteredDeviceId());
    }

    public boolean changeRegisteredDevice(Long userId, String newDeviceId) {
        // Kiểm tra user có tồn tại không
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy người dùng với ID: " + userId);
        }

        User user = userOpt.get();

        // Kiểm tra nếu deviceId mới giống với device cũ
        if (newDeviceId.equals(user.getRegisteredDeviceId())) {
            throw new RuntimeException("Thiết bị mới trùng với thiết bị hiện tại");
        }

        // Kiểm tra nếu deviceId đã được đăng ký cho user khác
        if (userRepository.existsByRegisteredDeviceIdAndIdNot(newDeviceId, userId)) {
            throw new RuntimeException("Thiết bị này đã được đăng ký cho tài khoản khác");
        }

        // Cập nhật thông tin thiết bị mới
        user.setRegisteredDeviceId(newDeviceId);
        user.setDeviceRegisteredAt(LocalDateTime.now());
        userRepository.save(user);

        return true;
    }

    public String getRegisteredDeviceId(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }

        return userOpt.get().getRegisteredDeviceId();
    }
}