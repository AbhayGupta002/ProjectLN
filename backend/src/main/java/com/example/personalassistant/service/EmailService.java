package com.example.personalassistant.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtp(String toEmail, String otp) {
        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your OTP Code");
            message.setText("Your OTP is: " + otp + "\nIt is valid for 5 minutes.");

            mailSender.send(message);
            org.slf4j.LoggerFactory.getLogger(EmailService.class).info("Email sent successfully to {}", toEmail);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(EmailService.class).warn("Failed to deliver email to {}: {}. [DEV/TEST OTP FALLBACK: {}]", toEmail, e.getMessage(), otp);
        }
    }
}
