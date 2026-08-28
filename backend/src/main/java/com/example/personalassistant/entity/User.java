package com.example.personalassistant.entity;

import java.util.List;

import com.example.personalassistant.enums.AccountEnum;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@ToString(exclude = {"bookings", "tourBookings"})
@EqualsAndHashCode(exclude = {"bookings", "tourBookings"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "mobile", unique = true)
    private String mobile;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "city")
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "accountstatus")
    private AccountEnum accountEnum;

    // HOTEL BOOKINGS
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Booking> bookings;

    // TOUR BOOKINGS ✅
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<TourBooking> tourBookings;
}
