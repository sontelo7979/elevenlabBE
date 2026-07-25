package com.example.demo.service;

import com.example.demo.dto.DeviceStatusResponse;
import com.example.demo.model.DeviceRequest;
import com.example.demo.repository.DeviceRequestRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DeviceRequestService {

    private final DeviceRequestRepository deviceRequestRepository;

    public DeviceRequestService(DeviceRequestRepository deviceRequestRepository) {
        this.deviceRequestRepository = deviceRequestRepository;
    }

    /**
     * API 1: Gửi yêu cầu kích hoạt thiết bị.
     * - Nếu fingerprint chưa tồn tại → tạo mới với status = "pending"
     * - Nếu đã tồn tại → trả về trạng thái hiện tại
     */
    public DeviceStatusResponse submitDeviceRequest(String fingerprint) {
        Optional<DeviceRequest> existing = deviceRequestRepository.findByFingerprint(fingerprint);

        if (existing.isPresent()) {
            DeviceRequest device = existing.get();
            String status = device.getStatus();

            return switch (status) {
                case "approved" -> new DeviceStatusResponse("approved", "Thiết bị đã được kích hoạt.");
                case "rejected" -> new DeviceStatusResponse("rejected", "Thiết bị bị từ chối.");
                default -> new DeviceStatusResponse("pending", "Yêu cầu đã được ghi nhận, chờ admin duyệt.");
            };
        }

        // Chưa tồn tại → tạo bản ghi mới
        DeviceRequest newRequest = DeviceRequest.builder()
                .fingerprint(fingerprint)
                .status("pending")
                .build();
        deviceRequestRepository.save(newRequest);

        return new DeviceStatusResponse("pending", "Yêu cầu đã được ghi nhận, chờ admin duyệt.");
    }

    /**
     * API 2: Kiểm tra trạng thái duyệt của fingerprint
     */
    public DeviceStatusResponse checkDeviceStatus(String fingerprint) {
        Optional<DeviceRequest> existing = deviceRequestRepository.findByFingerprint(fingerprint);

        if (existing.isEmpty()) {
            return new DeviceStatusResponse("not_found", "Không tìm thấy yêu cầu cho fingerprint này.");
        }

        DeviceRequest device = existing.get();
        String status = device.getStatus();

        return switch (status) {
            case "approved" -> new DeviceStatusResponse("approved", "Thiết bị đã được duyệt.");
            case "rejected" -> new DeviceStatusResponse("rejected", "Thiết bị bị từ chối.");
            default -> new DeviceStatusResponse("pending", "Đang chờ duyệt...");
        };
    }

    /**
     * API 3: Admin phê duyệt hoặc từ chối thiết bị
     */
    public DeviceStatusResponse updateDeviceStatus(String fingerprint, String action) {
        Optional<DeviceRequest> existing = deviceRequestRepository.findByFingerprint(fingerprint);

        if (existing.isEmpty()) {
            return null; // Controller sẽ trả 404
        }

        DeviceRequest device = existing.get();

        switch (action.toLowerCase()) {
            case "approve" -> device.setStatus("approved");
            case "reject" -> device.setStatus("rejected");
            default -> {
                return new DeviceStatusResponse("error", "Action không hợp lệ. Chỉ chấp nhận 'approve' hoặc 'reject'.");
            }
        }

        deviceRequestRepository.save(device);
        return new DeviceStatusResponse(device.getStatus(), "Cập nhật thành công");
    }
}
