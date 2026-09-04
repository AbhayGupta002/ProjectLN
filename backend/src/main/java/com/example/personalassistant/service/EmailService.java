package com.example.personalassistant.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendOtp(String toEmail, String otp) {
        log.info("Sending registration verification OTP to {}: [CODE: {}]", toEmail, otp);
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. OTP fallback for {}: {}", toEmail, otp);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your WorldTours Verification Code");
            message.setText("Welcome to WorldTours!\n\nYour 6-digit registration verification code is: " + otp
                    + "\n\nThis code is valid for 5 minutes. Please do not share this code with anyone.");

            mailSender.send(message);
            log.info("OTP email delivered successfully to {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to deliver email to {}: {}. [FALLBACK CODE: {}]", toEmail, e.getMessage(), otp);
        }
    }

    public void sendNewPassword(String toEmail, String newPassword) {
        log.info("Sending password reset email to {}", toEmail);
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. Temporary password for {}: {}", toEmail, newPassword);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your New Password - WorldTours Account");
            message.setText("Hello,\n\nWe received a password reset request for your WorldTours account."
                    + "\n\nYour new password is: " + newPassword
                    + "\n\nPlease use this new password to log in. We recommend updating your password once logged in via your profile settings."
                    + "\n\nBest regards,\nWorldTours Security Team");

            mailSender.send(message);
            log.info("New password sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to deliver password email to {}: {}. [FALLBACK PASSWORD: {}]", toEmail, e.getMessage(), newPassword);
        }
    }
}
