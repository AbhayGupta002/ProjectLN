package com.example.personalassistant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "admin")
public class Admin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "adminname")
    private String name;

    @Column(name = "number")
    private Long number;

    @Column(name = "email", nullable = false ,updatable = false)
    private String email;

    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;



}

