package com.example.personalassistant.dto;

import lombok.Data;

@Data
public class ChangePasswordDto {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
