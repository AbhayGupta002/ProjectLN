package com.example.personalassistant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "otp_verification")
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "otp")
    private String otp;

    @Column(name = "exptime")
    private LocalDateTime expiryTime;

    @Column(name = "verified")
    private boolean verified;
}
