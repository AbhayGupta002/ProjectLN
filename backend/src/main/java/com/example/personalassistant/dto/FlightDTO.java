package com.example.personalassistant.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class FlightDTO {
    private String flightNumber;
    private String airline;
    private String source;
    private String destination;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private Double fare;
    private String flightClass;
    private String amenities;
    private String imageUrl;
}
