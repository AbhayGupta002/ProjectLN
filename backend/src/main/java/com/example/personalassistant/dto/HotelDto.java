package com.example.personalassistant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelDto {
    private Long id;
    private String hotel;
    private String email;
    private String mobile;
    private String password;
    private String address;
    private String city;
    private String price;
    private double latitude;      // NEW — map coordinate
    private double longitude;
    private String status;
    private String roomAvailable;
    private String location;
}
