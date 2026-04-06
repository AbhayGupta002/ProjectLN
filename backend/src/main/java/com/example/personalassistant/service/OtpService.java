package com.example.personalassistant.service;

import com.example.personalassistant.dto.ErrorDetails;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.entity.OtpVerification;
import com.example.personalassistant.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    public OtpVerification sendOtp(String email) {
        Response response = new Response();

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        OtpVerification otpEntity = new OtpVerification();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpEntity.setVerified(false);

        OtpVerification otpVerification = otpRepository.save(otpEntity);

        emailService.sendOtp(email, otp);
        return null;
    }

    public boolean verifyOtp(String email, String otp) {

        Response response = new Response();
        OtpVerification otpEntity = otpRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            ErrorDetails errorDetails = new ErrorDetails(HttpStatus.REQUEST_TIMEOUT
            ,"OTP expired");
            response.setError(errorDetails);
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(response).hasBody();
        }

        if (!otpEntity.getOtp().equals(otp)) {
            ErrorDetails errorDetails = new ErrorDetails(HttpStatus.NOT_FOUND
                    ,"Invalid OTP");
            response.setError(errorDetails);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response).hasBody();

        }

        otpEntity.setVerified(true);
        otpRepository.save(otpEntity);

        return true;
    }
}