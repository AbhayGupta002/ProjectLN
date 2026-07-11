package com.example.personalassistant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourGuideDTO {
    private Long id;

    private String guideName;

    private String email;

    private String phone;

    private String gender;

    private Integer experience;

    private String languages;

    private String specialization;

    private String description;

    private String imageUrl;

    private Double pricePerDay;

    private Double rating;

    private Boolean available;

}
