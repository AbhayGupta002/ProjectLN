package com.example.personalassistant.service;

import com.example.personalassistant.dto.OtpVerificationResult;
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
        if (email == null || email.isBlank()) {
            return null;
        }

        String normalizedEmail = email.trim().toLowerCase();
        String otp = String.format("%06d", new Random().nextInt(900000) + 100000);

        try {
            otpRepository.deleteByEmail(normalizedEmail);
            otpRepository.deleteByEmail(email.trim());
        } catch (Exception ignored) {}

        OtpVerification otpEntity = new OtpVerification();
        otpEntity.setEmail(normalizedEmail);
        otpEntity.setOtp(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpEntity.setVerified(false);
        otpEntity.setFailedAttempts(0);

        otpRepository.save(otpEntity);

        try {
            emailService.sendOtp(normalizedEmail, otp);
        } catch (Exception ignored) {}

        return otp;
    }

    public OtpVerification sendOtp(String email) {
        generateAndSendOtp(email);
        return null;
    }

    @Transactional
    public OtpVerificationResult verifyOtpWithResult(String email, String otp) {
        if (email == null || email.isBlank() || otp == null || otp.trim().isEmpty()) {
            return new OtpVerificationResult(false, "Email and verification code are required.", 0, false);
        }

        String normalizedEmail = email.trim().toLowerCase();
        OtpVerification otpEntity = otpRepository.findTopByEmailOrderByIdDesc(normalizedEmail)
                .or(() -> otpRepository.findTopByEmailOrderByIdDesc(email.trim()))
                .orElse(null);

        if (otpEntity == null) {
            return new OtpVerificationResult(false, "No verification code found for this email. Please request a new code.", 0, false);
        }

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            return new OtpVerificationResult(false, "Verification code has expired. Please request a new code.", 0, false);
        }

        if (otpEntity.getFailedAttempts() >= 3) {
            return new OtpVerificationResult(false, "Maximum verification attempts (3/3) exceeded. This code has been invalidated for security. Please request a new code.", 0, true);
        }

        if (!otpEntity.getOtp().trim().equals(otp.trim())) {
            int currentFailed = otpEntity.getFailedAttempts() + 1;
            otpEntity.setFailedAttempts(currentFailed);
            int remaining = 3 - currentFailed;

            if (remaining <= 0) {
                // Invalidate this OTP immediately to prevent brute force
                otpEntity.setExpiryTime(LocalDateTime.now().minusSeconds(1));
                otpRepository.save(otpEntity);
                return new OtpVerificationResult(false, "Incorrect verification code. Maximum attempts (3/3) exceeded. This OTP has been invalidated for security. Please request a new code.", 0, true);
            }

            otpRepository.save(otpEntity);
            return new OtpVerificationResult(false, "Incorrect verification code. You have " + remaining + " attempt(s) remaining.", remaining, false);
        }

        otpEntity.setVerified(true);
        otpRepository.save(otpEntity);

        return new OtpVerificationResult(true, "OTP Verified Successfully", 3, false);
    }

    @Transactional
    public boolean verifyOtp(String email, String otp) {
        OtpVerificationResult result = verifyOtpWithResult(email, otp);
        return result.isSuccess();
    }
}