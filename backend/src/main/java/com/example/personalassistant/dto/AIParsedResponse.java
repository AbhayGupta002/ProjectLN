package com.example.personalassistant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIParsedResponse {

    private String intent;
    private String location;
    private String hotelName;
    private Long date;
    private Long userId;
    private Long bookingId;
    private String city;
    private String email;
    private String password;
    private String phoneNumber;
    private Integer budget;
    private String chatResponse;
    private String calculation;
    private int days;
    private String message;

}