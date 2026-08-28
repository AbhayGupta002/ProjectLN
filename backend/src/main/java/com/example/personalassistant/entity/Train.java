package com.example.personalassistant.entity;

import lombok.Data;
import java.time.LocalTime;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "train", indexes = {
        @Index(name = "idx_train_route", columnList = "source, destination"),
        @Index(name = "idx_train_status", columnList = "status")
})
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String trainNumber;

    @Column(nullable = false)
    private String trainName;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private LocalTime departureTime;

    @Column(nullable = false)
    private LocalTime arrivalTime;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer availableSeats;

    @Column(nullable = false)
    private Double fare;

    private String trainClass;

    @Column(length = 3000)
    private String amenities;

    private String imageUrl;

    private Boolean status = true;

    @Version
    private Long version;
}
