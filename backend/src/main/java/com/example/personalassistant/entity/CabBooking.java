package com.example.personalassistant.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.example.personalassistant.enums.BookingStatus;
import com.example.personalassistant.enums.PaymentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cab_bookings", indexes = {
        @Index(name = "idx_cb_user", columnList = "userId"),
        @Index(name = "idx_cb_cab", columnList = "cabId"),
        @Index(name = "idx_cb_status", columnList = "bookingStatus")
})
public class CabBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long cabId;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerPhone;

    @Column(nullable = false)
    private String pickupLocation;

    @Column(nullable = false)
    private String dropLocation;

    private LocalDateTime bookingDate;

    @Column(nullable = false)
    private LocalDateTime pickupTime;

    @Column(nullable = false)
    private Double estimatedFare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus bookingStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Version
    private Long version;
}
