package com.example.demo.repository;

import com.example.demo.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Tìm customer theo email (chính xác)
    Optional<Customer> findByEmail(String email);

    // Tìm customer theo email (không phân biệt hoa thường)
    Optional<Customer> findByEmailIgnoreCase(String email);

    // Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);

    // Tìm customer đang hoạt động theo email
    Optional<Customer> findByEmailAndIsActiveTrue(String email);

    // Tìm kiếm customer theo email chứa keyword
    @Query("SELECT c FROM Customer c WHERE LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<Customer> searchByEmailContaining(@Param("email") String email);

    // Tìm customer với phân trang và lọc
    @Query("SELECT c FROM Customer c WHERE " +
            "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:isActive IS NULL OR c.isActive = :isActive)")
    Page<Customer> searchCustomers(@Param("email") String email,
                                   @Param("name") String name,
                                   @Param("isActive") Boolean isActive,
                                   Pageable pageable);

    // Lấy customer hết hạn trong khoảng thời gian
    @Query("SELECT c FROM Customer c WHERE c.endDate BETWEEN :start AND :end AND c.isActive = true")
    List<Customer> findExpiringCustomers(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    // Lấy tất cả customer đang hoạt động
    List<Customer> findByIsActiveTrue();

    // Lấy customer theo danh sách email
    List<Customer> findByEmailIn(List<String> emails);
}