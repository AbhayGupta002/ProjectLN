package com.example.personalassistant.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class TrainDTO {
    private String trainNumber;
    private String trainName;
    private String source;
    private String destination;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private Double fare;
    private String trainClass;
    private String amenities;
    private String imageUrl;
}
