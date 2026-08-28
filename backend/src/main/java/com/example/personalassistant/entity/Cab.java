package com.example.personalassistant.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cabs", indexes = {
        @Index(name = "idx_cab_city", columnList = "city"),
        @Index(name = "idx_cab_available", columnList = "available")
})
public class Cab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String vehicleNumber;

    @Column(nullable = false)
    private String driverName;

    @Column(nullable = false)
    private String driverPhone;

    @Column(nullable = false)
    private String carModel;

    @Column(nullable = false)
    private String carType; // SEDAN, SUV, HATCHBACK

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Double perKmRate;

    @Column(nullable = false)
    private Double baseFare;

    @Column(nullable = false)
    private Boolean available = true;

    @Version
    private Long version;
}
