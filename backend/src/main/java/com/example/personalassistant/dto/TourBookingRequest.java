package com.example.personalassistant.dto;

import lombok.Data;

@Data
public class TourBookingRequest {
    private String name;
    private Long userId;
    private String email;
    private Long hotelId;
    private String destination;
    private String city;
    private String checkInDate;
    private String checkOutDate;
    private int totalGuests;
    private double price;

}
