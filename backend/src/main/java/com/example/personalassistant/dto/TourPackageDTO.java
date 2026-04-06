package com.example.personalassistant.dto;

import com.example.personalassistant.enums.TourEnum;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourPackageDTO {

    private Long id;
    private String title;
    private String email; //hotel login mail
    private double price;
    private String imageUrl;
    private String location;
    private String password;
    private int durationDays;
    private String description;
    private TourEnum tourStatus;
    private LocalDateTime localDateTime;
}
