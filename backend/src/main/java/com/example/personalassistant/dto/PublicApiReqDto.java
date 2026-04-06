package com.example.personalassistant.dto;

import com.example.personalassistant.enums.TourEnum;
import lombok.Data;

import java.time.temporal.Temporal;

@Data
public class PublicApiReqDto {
    private Temporal checkIn;
    private Temporal checkOut;
    private int days;
    private String title;
    private String email; //hotel login mail
    private double price;
    private String imageUrl;
    private String location;
    private String password;
    private int durationDays;
    private String description;
    private TourEnum tourStatus;
}
