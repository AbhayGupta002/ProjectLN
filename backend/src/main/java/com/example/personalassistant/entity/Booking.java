package com.example.personalassistant.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.example.personalassistant.enums.BookingStatus;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Data
@NoArgsConstructor
@ToString(exclude = {"user", "hotel"})
@EqualsAndHashCode(exclude = {"user", "hotel"})
@Entity
@Table(name = "bookings",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "hotel_id"})
}
)
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)  // Changed from "id" to "user_id"
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)  // Changed from "id" to "hotel_id"
    @JsonIgnore
    private Hotel hotel;

    @Column(name = "checkin", nullable = false)
    private LocalDateTime checkIn;

    @Column(name = "checkout", nullable = false)
    private LocalDateTime checkOut;

    @Column(name = "room", nullable = false)
    private Integer roomsNumber;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "payment")
    private String paymentStatus;

    @Column(name = "booking_status")
    private BookingStatus bookingStatus;
}