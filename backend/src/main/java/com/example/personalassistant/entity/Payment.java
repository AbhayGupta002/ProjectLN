package com.example.personalassistant.entity;

import com.example.personalassistant.enums.PaymentStatus;
import lombok.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payments", indexes = {
        @Index(name = "idx_pay_user_email", columnList = "user_email"),
        @Index(name = "idx_pay_order_id", columnList = "razorpay_order_id"),
        @Index(name = "idx_pay_status", columnList = "status"),
        @Index(name = "idx_pay_booking", columnList = "booking_type, booking_id")
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "booking_type", nullable = false)
    private String bookingType; // HOTEL, TOUR, FLIGHT, BUS, TRAIN, CAB

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "description")
    private String description;

    // FK to TourBooking (optional/nullable for other booking types)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = true)
    private TourBooking booking;

    @Column(name = "razorpay_order_id", unique = true)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", unique = true)
    private String razorpayPaymentId;

    @Column(name = "razorpay_refund_id")
    private String razorpayRefundId;

    @Column(name = "upi_vpa")
    private String upiVpa;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "payment_method")
    private String paymentMethod; // CARD, UPI, QR_CODE, NETBANKING

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // IMMUTABLE AUDIT GUARD: Transaction history should never be deleted
    @PreRemove
    public void preventDeletion() {
        throw new UnsupportedOperationException("SECURITY AUDIT VIOLATION: Transaction history records cannot be deleted from the database.");
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (currency == null) {
            currency = "INR";
        }
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
