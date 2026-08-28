package com.example.personalassistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.example.personalassistant.enums.BookingStatus;
import com.example.personalassistant.enums.PaymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "flight_booking", indexes = {
        @Index(name = "idx_fb_user", columnList = "userId"),
        @Index(name = "idx_fb_flight", columnList = "flightId"),
        @Index(name = "idx_fb_status", columnList = "bookingStatus")
})
public class FlightBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long flightId;

    @Column(nullable = false)
    private String passengerName;

    @Column(nullable = false)
    private Integer passengerAge;

    @Column(nullable = false)
    private String passengerGender;

    @Column(nullable = false)
    private Integer numberOfSeats;

    @Column(nullable = false)
    private Double totalFare;

    @Column(nullable = false)
    private LocalDate journeyDate;

    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDateTime bookingDate;

    @Version
    private Long version;

    public FlightBooking() {
    }
}
