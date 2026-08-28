package com.example.personalassistant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CabDTO {
    private Long id;
    private String vehicleNumber;
    private String driverName;
    private String driverPhone;
    private String carModel;
    private String carType;
    private String city;
    private Integer capacity;
    private Double perKmRate;
    private Double baseFare;
    private Boolean available;
}
