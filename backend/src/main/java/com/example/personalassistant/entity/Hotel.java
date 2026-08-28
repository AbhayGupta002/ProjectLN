package com.example.personalassistant.entity;


import lombok.*;
import java.util.List;
import java.util.ArrayList;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.personalassistant.enums.AccountEnum;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hotels", indexes = {
        @Index(name = "idx_hotel_city", columnList = "city"),
        @Index(name = "idx_hotel_status", columnList = "status"),
        @Index(name = "idx_hotel_name", columnList = "hotel"),
        @Index(name = "idx_hotel_location", columnList = "hotellocation")
})
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel", nullable = false)
    private String hotel;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "price", nullable = false)
    private String price;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "roomavl")
    private String availableRooms;

    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "longitude")
    private double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountEnum accountEnum;

    // 1 HOTEL → MANY TOUR PACKAGES
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore   // 👈 ADD THIS while result in lopp or infinite
    private List<TourPackage> tourPackages = new ArrayList<>();

    // ⭐ 1 HOTEL → MANY BOOKINGS (IMPORTANT)
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Booking> bookings = new ArrayList<>();

    @Column(name = "hotellocation")
    private String location;

    @Version
    private Long version;
}
