package com.example.personalassistant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationResult {
    private boolean success;
    private String message;
    private int remainingAttempts;
    private boolean locked;
}
