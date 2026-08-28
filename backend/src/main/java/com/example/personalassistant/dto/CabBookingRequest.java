package com.example.personalassistant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CabBookingRequest {
    private Long cabId;
    private String customerName;
    private String customerPhone;
    private String pickupLocation;
    private String dropLocation;
    private LocalDateTime pickupTime;
    private Double estimatedDistanceKm;
}
