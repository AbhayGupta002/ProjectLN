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
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_type")
    private String bookingType; // HOTEL, TOUR, FLIGHT, BUS, TRAIN, CAB

    @Column(name = "booking_id")
    private Long bookingId;

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

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "payment_method")
    private String paymentMethod; // CARD, UPI, NETBANKING

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
