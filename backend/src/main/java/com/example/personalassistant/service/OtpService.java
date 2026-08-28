package com.example.personalassistant.service;

import com.example.personalassistant.entity.OtpVerification;
import com.example.personalassistant.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public String generateAndSendOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(900000) + 100000);

        try {
            otpRepository.deleteByEmail(email);
        } catch (Exception ignored) {}

        OtpVerification otpEntity = new OtpVerification();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpEntity.setVerified(false);

        otpRepository.save(otpEntity);

        try {
            emailService.sendOtp(email, otp);
        } catch (Exception ignored) {}

        return otp;
    }

    public OtpVerification sendOtp(String email) {
        generateAndSendOtp(email);
        return null;
    }

    @Transactional
    public boolean verifyOtp(String email, String otp) {
        if (email == null || otp == null || otp.trim().isEmpty()) {
            return false;
        }

        OtpVerification otpEntity = otpRepository.findTopByEmailOrderByIdDesc(email).orElse(null);
        if (otpEntity == null) {
            return false;
        }

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            return false;
        }

        if (!otpEntity.getOtp().trim().equals(otp.trim())) {
            return false;
        }

        otpEntity.setVerified(true);
        otpRepository.save(otpEntity);

        return true;
    }
}