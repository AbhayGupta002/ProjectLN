package com.example.personalassistant.repository;

import com.example.personalassistant.entity.Payment;
import com.example.personalassistant.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    Optional<Payment> findByBookingTypeAndBookingId(String bookingType, Long bookingId);

    @Query("SELECT p FROM Payment p WHERE p.userEmail = :userEmail " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:bookingType IS NULL OR p.bookingType = :bookingType) " +
            "AND (:search IS NULL OR LOWER(p.razorpayPaymentId) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(p.razorpayOrderId) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY p.createdAt DESC")
    List<Payment> searchUserTransactions(
            @Param("userEmail") String userEmail,
            @Param("status") PaymentStatus status,
            @Param("bookingType") String bookingType,
            @Param("search") String search
    );

    // ===================================================================
    // AUDIT SECURITY: Physical deletion of financial transactions is blocked
    // ===================================================================
    @Override
    default void delete(Payment entity) {
        throw new UnsupportedOperationException("AUDIT SECURITY VIOLATION: Transaction records are permanent and cannot be deleted.");
    }

    @Override
    default void deleteById(Long id) {
        throw new UnsupportedOperationException("AUDIT SECURITY VIOLATION: Transaction records are permanent and cannot be deleted.");
    }

    @Override
    default void deleteAll() {
        throw new UnsupportedOperationException("AUDIT SECURITY VIOLATION: Transaction records are permanent and cannot be deleted.");
    }
}
