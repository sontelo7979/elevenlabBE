package com.example.demo.repository;

import com.example.demo.model.CustomerTransaction;
import com.example.demo.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CustomerTransactionRepository extends JpaRepository<CustomerTransaction, Long> {

    // Tìm transaction SUCCESS có nội dung CHỨA content
    @Query("SELECT t FROM CustomerTransaction t WHERE " +
            "t.content LIKE CONCAT('%', :content, '%') AND " +
            "t.amount = :amount AND " +
            "t.status = 'SUCCESS'")
    Optional<CustomerTransaction> findSuccessByContentContaining(
            @Param("content") String content,
            @Param("amount") BigDecimal amount);

    // Tìm transaction PENDING có nội dung CHỨA content
    @Query("SELECT t FROM CustomerTransaction t WHERE " +
            "t.content LIKE CONCAT('%', :content, '%') AND " +
            "t.amount = :amount AND " +
            "t.status = 'PENDING'")
    Optional<CustomerTransaction> findPendingByContentContaining(
            @Param("content") String content,
            @Param("amount") BigDecimal amount);

    // Kiểm tra đã tồn tại transaction SUCCESS với sepayId
    boolean existsBySepayTransactionIdAndStatus(String sepayTransactionId, TransactionStatus status);

    // Update transaction chỉ khi còn PENDING (tránh duplicate)
    @Modifying
    @Transactional
    @Query("UPDATE CustomerTransaction t SET " +
            "t.status = :status, " +
            "t.transactionId = :transactionId, " +
            "t.transactionDate = :transactionDate, " +
            "t.bankCode = :bankCode, " +
            "t.bankAccountNumber = :bankAccountNumber, " +
            "t.paymentType = :paymentType, " +
            "t.sepayTransactionId = :sepayTransactionId, " +
            "t.updatedAt = :now " +
            "WHERE t.id = :id AND t.status = 'PENDING'")
    int updatePendingTransaction(@Param("id") Long id,
                                 @Param("status") TransactionStatus status,
                                 @Param("transactionId") String transactionId,
                                 @Param("transactionDate") LocalDateTime transactionDate,
                                 @Param("bankCode") String bankCode,
                                 @Param("bankAccountNumber") String bankAccountNumber,
                                 @Param("paymentType") String paymentType,
                                 @Param("sepayTransactionId") String sepayTransactionId,
                                 @Param("now") LocalDateTime now);
}