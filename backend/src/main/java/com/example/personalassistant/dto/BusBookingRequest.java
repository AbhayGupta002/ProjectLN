package com.example.personalassistant.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BusBookingRequest {
    private Long userId;
    private Long busId;
    private String passengerName;
    private Integer passengerAge;
    private String passengerGender;
    private Integer numberOfSeats;
    private LocalDate journeyDate;
}
