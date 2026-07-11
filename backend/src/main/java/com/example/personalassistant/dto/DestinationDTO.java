package com.example.personalassistant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DestinationDTO {

    private Long id;

    private String destinationName;

    private String state;

    private String country;

    private String description;

    private String imageUrl;

    private String bestTimeToVisit;

    private Double averageBudget;

    private Double latitude;

    private Double longitude;

    private Boolean status;

}
