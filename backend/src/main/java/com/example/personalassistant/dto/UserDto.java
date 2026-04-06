package com.example.personalassistant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String name;
    private String email;
    private String mobile;
    private String password;
    private String hotel;
    private Long hotelid;
    private String hotelemail;
    private String tourBookingId;
    private String location;
    private String otp;
}
