package com.example.personalassistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "toker", nullable = false, unique = true)
    private String token;

    @Column(name = "exdate", nullable = false)
    private LocalDateTime expiryDate;

    @ManyToOne
    private UserLogin user;

}
