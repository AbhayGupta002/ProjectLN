package com.example.personalassistant.repository;

import java.util.Optional;
import com.example.personalassistant.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);
    Optional<Payment> findByBookingTypeAndBookingId(String bookingType, Long bookingId);
}

