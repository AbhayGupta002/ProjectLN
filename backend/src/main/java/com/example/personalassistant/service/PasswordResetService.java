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
        PasswordResetToken prt = tokenRepo.findByToken(token).orElseThrow(() -> new RuntimeException("Invalid token"));
        if (prt.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepo.delete(prt);
            throw new RuntimeException("Token expired");
        }
        UserLogin user = prt.getUser();
        user.setPassword(encoder.encode(newPassword));
        userLoginRepository.save(user);
        tokenRepo.delete(prt);
    }

    public boolean validateToken(String token) {
        Optional<PasswordResetToken> prt = tokenRepo.findByToken(token);
        return prt.isPresent() && prt.get().getExpiryDate().isAfter(LocalDateTime.now());
    }
}
