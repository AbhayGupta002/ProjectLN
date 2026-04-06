package com.example.personalassistant.entity;

import com.example.personalassistant.enums.TourEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import jakarta.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tour_packages")
public class TourPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "title")
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "price")
    private double price;

    @Column(name = "duration")
    private int durationDays;

    @Column(name = "location")
    private String location;

    @Column(name = "imageurl")
    private String imageUrl;

    @Column(name = "status")
    private TourEnum tourStatus;

    @ManyToOne
    @JoinColumn(name = "hotel_id") // ✅ new column in tour_packages table
    private Hotel hotel;


}
