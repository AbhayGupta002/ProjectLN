package com.example.personalassistant.entity;

import jakarta.persistence.*;
import jakarta.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usercomplaint")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userid")
    private Long userId;

    @Column(name = "bookingid")
    private Long bookingId;

    @Column(name = "complaint", nullable = false)
    private String message;

    @Column(name = "datetime")
    private LocalDateTime createdAt = LocalDateTime.now();

}
