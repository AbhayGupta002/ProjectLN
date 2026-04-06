package com.example.personalassistant.dto;

import lombok.Data;

@Data
public class UpdateHotelProfile {
    private String hotel;
    private String mobile;
    private String password;
    private String address;
    private String city;
    private String price;
    private double latitude;      // NEW — map coordinate
    private double longitude;
}
