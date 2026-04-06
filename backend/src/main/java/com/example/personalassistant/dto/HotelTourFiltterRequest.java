package com.example.personalassistant.dto;


import com.example.personalassistant.enums.AccountEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelTourFiltterRequest {
    private Double userLat;
    private Double userLng;

    private Double rangeKm; // e.g. 5, 10, 20

    private Boolean luxury; // true / false (optional)

    private String city;
    private String state;
    private AccountEnum status;

}
