package com.example.personalassistant.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${resend.api.key:}")
    private String resendApiKey;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    @Async
    public void sendOtp(String toEmail, String otp) {
        CompletableFuture.runAsync(() -> {
            log.info("Dispatching registration verification OTP to {}: [CODE: {}]", toEmail, otp);
            String subject = "Your WorldTours Verification Code";
            String body = "Welcome to WorldTours!\n\nYour 6-digit registration verification code is: " + otp
                    + "\n\nThis code is valid for 5 minutes. Please do not share this code with anyone.";

            boolean sent = trySendViaHttpsApis(toEmail, subject, body);
            if (!sent && mailSender != null) {
                trySendViaSmtp(toEmail, subject, body);
            }
        });
    }

    @Async
    public void sendNewPassword(String toEmail, String newPassword) {
        CompletableFuture.runAsync(() -> {
            log.info("Dispatching password reset email to {}", toEmail);
            String subject = "Your New Password - WorldTours Account";
            String body = "Hello,\n\nWe received a password reset request for your WorldTours account."
                    + "\n\nYour new password is: " + newPassword
                    + "\n\nPlease use this new password to log in. We recommend updating your password once logged in via your profile settings."
                    + "\n\nBest regards,\nWorldTours Security Team";

            boolean sent = trySendViaHttpsApis(toEmail, subject, body);
            if (!sent && mailSender != null) {
                trySendViaSmtp(toEmail, subject, body);
            }
        });
    }

    private boolean trySendViaHttpsApis(String toEmail, String subject, String body) {
        // 1. Try Resend API (HTTPS port 443 - works on Render Free tier)
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            try {
                String payload = String.format(
                        "{\"from\":\"WorldTours <onboarding@resend.dev>\",\"to\":[\"%s\"],\"subject\":\"%s\",\"text\":\"%s\"}",
                        toEmail,
                        escapeJson(subject),
                        escapeJson(body)
                );
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.resend.com/emails"))
                        .header("Authorization", "Bearer " + resendApiKey.trim())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                        .timeout(Duration.ofSeconds(4))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    log.info("Email delivered successfully via Resend API to {}", toEmail);
                    return true;
                } else {
                    log.warn("Resend API response {}: {}", response.statusCode(), response.body());
                }
            } catch (Exception e) {
                log.warn("Resend API delivery failed: {}", e.getMessage());
            }
        }

        // 2. Try Brevo API (HTTPS port 443 - works on Render Free tier)
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                String payload = String.format(
                        "{\"sender\":{\"name\":\"WorldTours\",\"email\":\"noreply@worldtours.com\"},\"to\":[{\"email\":\"%s\"}],\"subject\":\"%s\",\"textContent\":\"%s\"}",
                        toEmail,
                        escapeJson(subject),
                        escapeJson(body)
                );
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                        .header("api-key", brevoApiKey.trim())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                        .timeout(Duration.ofSeconds(4))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    log.info("Email delivered successfully via Brevo API to {}", toEmail);
                    return true;
                } else {
                    log.warn("Brevo API response {}: {}", response.statusCode(), response.body());
                }
            } catch (Exception e) {
                log.warn("Brevo API delivery failed: {}", e.getMessage());
            }
        }

        return false;
    }

    private void trySendViaSmtp(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (mailUsername != null && !mailUsername.isBlank()) {
                message.setFrom(mailUsername);
            } else {
                message.setFrom("support@worldtours.com");
            }
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email delivered successfully via SMTP to {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to deliver email via SMTP to {}: {}", toEmail, e.getMessage());
        }
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
