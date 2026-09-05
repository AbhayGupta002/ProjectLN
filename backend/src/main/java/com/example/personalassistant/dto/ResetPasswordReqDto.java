package com.example.personalassistant.dto;

import lombok.Data;

@Data
public class ResetPasswordReqDto {
    private String token;
    private String newPassword;
    private String password;

    public String getNewPassword() {
        if (newPassword != null && !newPassword.isBlank()) return newPassword;
        return password;
    }
}
