package com.example.personalassistant.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tour_guide")
public class TourGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String guideName;

    @Column(unique = true)
    private String email;

    private String phone;

    private String gender;

    private Integer experience;

    private String languages;

    private String city;

    private String specialization;

    @Column(length = 3000)
    private String description;

    private String imageUrl;

    private Double pricePerDay;

    private Double rating = 0.0;

    private Boolean available = true;

}