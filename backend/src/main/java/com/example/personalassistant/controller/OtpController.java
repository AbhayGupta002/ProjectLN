package com.example.personalassistant.controller;

import com.example.personalassistant.service.OtpService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/verify")
    public String verifyOtp(@RequestParam String email,
                            @RequestParam String otp) {

        boolean isVerifiedOtp = otpService.verifyOtp(email, otp);

        if (isVerifiedOtp) {
            return "OTP Verified Successfully";
        }
        return "Invalid OTP";
    }
}
