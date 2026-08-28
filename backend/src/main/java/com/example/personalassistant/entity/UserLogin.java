package com.example.personalassistant.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credential")
public class UserLogin {

    @Id
    @Column(name = "email", nullable = false, updatable = true)
    @NotNull
    private String email;

    @Column(name = "password")
    private String Password;

    @Column
    private boolean emailVerified;

    @Column(name = "failed_attempts")
    private int failedLoginAttempts = 0;

    @Column(name = "account_locked")
    private boolean accountLocked = false;

    @Column(name = "two_factor_enabled")
    private boolean twoFactorEnabled = false;

    @Column(name = "two_factor_otp")
    private String twoFactorOtp;

    @Column(name = "two_factor_expiry")
    private java.time.LocalDateTime twoFactorOtpExpiry;

    @jakarta.persistence.Version
    private Long version;
}
