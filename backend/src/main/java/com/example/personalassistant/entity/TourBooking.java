package com.example.personalassistant.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.example.personalassistant.enums.BookingStatus;


@Entity
@Table(name = "tour_booking", indexes = {
        @Index(name = "idx_tb_user", columnList = "user_id"),
        @Index(name = "idx_tb_hotel", columnList = "hotel_id"),
        @Index(name = "idx_tb_destination", columnList = "destination"),
        @Index(name = "idx_tb_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tour_id", updatable = false)
    private Long tourId;

    // 🔗 RELATION TO USER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "hotel_id", nullable = false, updatable = false)
    private Long hotelId;

    // Snapshot fields (optional but OK)
    @Column(name = "name")
    private String name;

    @Column(name = "city")
    private String city;

    @Column(name = "email")
    private String email;

    @Column(name = "destination")
    private String destination;

    @Column(name = "checkin")
    private String checkInDate;

    @Column(name = "total_guest")
    private int totalGuests;

    @Column(name = "price")
    private double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status")
    private BookingStatus status;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Version
    private Long version;
}
