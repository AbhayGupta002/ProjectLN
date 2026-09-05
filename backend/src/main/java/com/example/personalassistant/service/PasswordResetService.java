package com.example.personalassistant.service;

import java.util.UUID;
import java.util.Optional;
import java.time.LocalDateTime;
import com.example.personalassistant.entity.User;
import com.example.personalassistant.entity.UserLogin;
import com.example.personalassistant.repository.UserLoginRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import com.example.personalassistant.repository.UserRepository;
import com.example.personalassistant.entity.PasswordResetToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.example.personalassistant.repository.PasswordResetTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserLoginRepository userLoginRepository;

    @Autowired
    private JavaMailSender mailSender;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Transactional
    public void requestPasswordResetByEmailOrMobile(String identifier) {
        Optional<UserLogin> userOpt = userLoginRepository.findByEmail(identifier);

        if (userOpt.isPresent()) {
            UserLogin user = userOpt.get();
            String token = UUID.randomUUID().toString();
            PasswordResetToken prt = new PasswordResetToken();
            prt.setToken(token);
            prt.setExpiryDate(LocalDateTime.now().plusHours(1));
            prt.setUser(user);
            tokenRepo.save(prt);

            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(user.getEmail());
                msg.setSubject("Password reset request");
                msg.setText("Reset link: http://localhost:3000/reset-password?token=" + token);
                mailSender.send(msg);
            }
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("token and password required");
        }

        Optional<PasswordResetToken> prtOpt = tokenRepo.findByToken(token);
        if (prtOpt.isPresent()) {
            PasswordResetToken prt = prtOpt.get();
            if (prt.getExpiryDate().isBefore(LocalDateTime.now())) {
                tokenRepo.delete(prt);
                throw new RuntimeException("Token expired");
            }
            UserLogin user = prt.getUser();
            if (user != null) {
                user.setPassword(encoder.encode(newPassword));
                user.setAccountLocked(false);
                user.setFailedLoginAttempts(0);
                userLoginRepository.save(user);
            }
            tokenRepo.delete(prt);
            return;
        }

        User user = userRepo.findByResetToken(token);
        if (user != null) {
            if (user.getTokenExpiry() != null && user.getTokenExpiry().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Token expired");
            }
            UserLogin userLogin = userLoginRepository.findByEmail(user.getEmail()).orElse(null);
            if (userLogin != null) {
                userLogin.setPassword(encoder.encode(newPassword));
                userLogin.setAccountLocked(false);
                userLogin.setFailedLoginAttempts(0);
                userLoginRepository.save(userLogin);
            }
            user.setResetToken(null);
            user.setTokenExpiry(null);
            userRepo.save(user);
            return;
        }

        throw new RuntimeException("Invalid token");
    }

    public boolean validateToken(String token) {
        Optional<PasswordResetToken> prt = tokenRepo.findByToken(token);
        return prt.isPresent() && prt.get().getExpiryDate().isAfter(LocalDateTime.now());
    }
}
