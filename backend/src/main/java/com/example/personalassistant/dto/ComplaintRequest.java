package com.example.personalassistant.dto;

import lombok.Data;

@Data
public class ComplaintRequest {
    private Long userId;
    private Long bookingId;
    private String message;
}
