package com.example.personalassistant.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class BusDTO {
    private String busName;
    private String busNumber;
    private String operatorName;
    private String source;
    private String destination;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private Double fare;
    private String busType;
    private String amenities;
    private String imageUrl;
    private Boolean status;
}
