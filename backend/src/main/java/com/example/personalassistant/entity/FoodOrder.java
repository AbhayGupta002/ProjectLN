package com.example.personalassistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "food_orders")
public class FoodOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Booking booking;

    @Column(columnDefinition = "TEXT")
    private String itemsJson;

    @Column(name = "total", nullable = false)
    private Double total;

    @Column(name = "orderedtime", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "status", nullable = false)
    private String status;
}
