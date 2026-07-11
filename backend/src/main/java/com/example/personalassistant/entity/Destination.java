package com.example.personalassistant.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "destination")
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String destinationName;

    private String state;

    private String country;

    @Column(length = 3000)
    private String description;

    private String imageUrl;

    private String bestTimeToVisit;

    private Double averageBudget;

    private Double latitude;

    private Double longitude;

    private Boolean status = true;
}