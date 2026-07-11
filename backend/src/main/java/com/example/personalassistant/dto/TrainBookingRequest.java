package com.example.personalassistant.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TrainBookingRequest {
    private Long userId;
    private Long trainId;
    private String passengerName;
    private Integer passengerAge;
    private String passengerGender;
    private Integer numberOfSeats;
    private LocalDate journeyDate;
}
