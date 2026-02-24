package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CollaboratorDTO;
import com.example.demo.dto.CreateCollaboratorRequest;
import com.example.demo.model.Collaborator;
import com.example.demo.model.User;
import com.example.demo.repository.CollaboratorRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/collaborators")
@RequiredArgsConstructor
public class CollaboratorController {

    private final CollaboratorRepository collaboratorRepository;
    private final UserRepository userRepository;

    /**
     * API tạo collaborator mới - CHỈ ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CollaboratorDTO>> createCollaborator(
            @RequestBody CreateCollaboratorRequest request) {
        try {
            // Kiểm tra user có tồn tại không
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + request.getUserId()));

            // Kiểm tra user đã là collaborator chưa
            if (collaboratorRepository.findByUserId(request.getUserId()).isPresent()) {
                throw new RuntimeException("User này đã là cộng tác viên");
            }

            // Tạo collaborator mới
            Collaborator collaborator = Collaborator.builder()
                    .user(user)
                    .commissionRate(request.getCommissionRate() != null ? request.getCommissionRate() : 0.1)
                    .totalCommission(0.0)
                    .phoneNumber(request.getPhoneNumber())
                    .bankName(request.getBankName())
                    .bankAccountNumber(request.getBankAccountNumber())
                    .bankAccountName(request.getBankAccountName())
                    .notes(request.getNotes())
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Collaborator savedCollaborator = collaboratorRepository.save(collaborator);

            // Tạo DTO để trả về
            CollaboratorDTO dto = CollaboratorDTO.builder()
                    .id(savedCollaborator.getId())
                    .userId(savedCollaborator.getUser().getId())
                    .username(savedCollaborator.getUser().getUsername())
                    .email(savedCollaborator.getUser().getEmail())
                    .commissionRate(savedCollaborator.getCommissionRate())
                    .totalCommission(savedCollaborator.getTotalCommission())
                    .phoneNumber(savedCollaborator.getPhoneNumber())
                    .bankName(savedCollaborator.getBankName())
                    .bankAccountNumber(savedCollaborator.getBankAccountNumber())
                    .bankAccountName(savedCollaborator.getBankAccountName())
                    .notes(savedCollaborator.getNotes())
                    .isActive(savedCollaborator.getIsActive())
                    .createdAt(savedCollaborator.getCreatedAt())
                    .updatedAt(savedCollaborator.getUpdatedAt())
                    .build();

            ApiResponse<CollaboratorDTO> response = new ApiResponse<>(
                    HttpStatus.CREATED.value(),
                    "Tạo cộng tác viên thành công",
                    dto
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            ApiResponse<CollaboratorDTO> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Tạo cộng tác viên thất bại: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API lấy danh sách tất cả collaborator - CHỈ ADMIN
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Collaborator>>> getAllCollaborators() {
        try {
            List<Collaborator> collaborators = collaboratorRepository.findAll();
            ApiResponse<List<Collaborator>> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy danh sách cộng tác viên thành công",
                    collaborators
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<Collaborator>> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lấy danh sách thất bại: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API lấy collaborator theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Collaborator>> getCollaboratorById(@PathVariable Long id) {
        try {
            Collaborator collaborator = collaboratorRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cộng tác viên với ID: " + id));

            ApiResponse<Collaborator> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy thông tin cộng tác viên thành công",
                    collaborator
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Collaborator> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lấy thông tin thất bại: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API lấy collaborator theo userId
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Collaborator>> getCollaboratorByUserId(@PathVariable Long userId) {
        try {
            Collaborator collaborator = collaboratorRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cộng tác viên với User ID: " + userId));

            ApiResponse<Collaborator> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy thông tin cộng tác viên thành công",
                    collaborator
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Collaborator> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lấy thông tin thất bại: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API cập nhật thông tin collaborator
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Collaborator>> updateCollaborator(
            @PathVariable Long id,
            @RequestBody CreateCollaboratorRequest request) {
        try {
            Collaborator collaborator = collaboratorRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cộng tác viên với ID: " + id));

            // Cập nhật thông tin
            if (request.getCommissionRate() != null) {
                collaborator.setCommissionRate(request.getCommissionRate());
            }
            if (request.getPhoneNumber() != null) {
                collaborator.setPhoneNumber(request.getPhoneNumber());
            }
            if (request.getBankName() != null) {
                collaborator.setBankName(request.getBankName());
            }
            if (request.getBankAccountNumber() != null) {
                collaborator.setBankAccountNumber(request.getBankAccountNumber());
            }
            if (request.getBankAccountName() != null) {
                collaborator.setBankAccountName(request.getBankAccountName());
            }
            if (request.getNotes() != null) {
                collaborator.setNotes(request.getNotes());
            }

            // Không cho phép thay đổi userId
            // Không cho phép thay đổi totalCommission ở đây

            Collaborator updatedCollaborator = collaboratorRepository.save(collaborator);

            ApiResponse<Collaborator> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Cập nhật thông tin cộng tác viên thành công",
                    updatedCollaborator
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<Collaborator> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Cập nhật thất bại: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API kích hoạt/vô hiệu hóa collaborator
     */
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Collaborator>> toggleCollaboratorStatus(@PathVariable Long id) {
        try {
            Collaborator collaborator = collaboratorRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cộng tác viên với ID: " + id));

            collaborator.setIsActive(!collaborator.getIsActive());
            Collaborator updatedCollaborator = collaboratorRepository.save(collaborator);

            String message = updatedCollaborator.getIsActive() ?
                    "Kích hoạt cộng tác viên thành công" :
                    "Vô hiệu hóa cộng tác viên thành công";

            ApiResponse<Collaborator> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    message,
                    updatedCollaborator
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<Collaborator> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Thay đổi trạng thái thất bại: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API xóa collaborator (soft delete - chỉ active/inactive, không xóa thật)
     * Nếu muốn xóa thật thì dùng DELETE, nhưng không khuyến khích
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteCollaborator(@PathVariable Long id) {
        try {
            Collaborator collaborator = collaboratorRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cộng tác viên với ID: " + id));

            // Soft delete - set inactive
            collaborator.setIsActive(false);
            collaboratorRepository.save(collaborator);

            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Xóa cộng tác viên thành công (đã chuyển sang trạng thái inactive)",
                    null
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Xóa thất bại: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}