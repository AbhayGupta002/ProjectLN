package com.example.personalassistant.dto;

import lombok.Data;

@Data
public class ResetPasswordReqDto {
    private String token;
    private String newPassword;
}
