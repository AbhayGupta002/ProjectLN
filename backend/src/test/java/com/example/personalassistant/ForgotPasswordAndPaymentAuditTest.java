package com.example.personalassistant;

import com.example.personalassistant.entity.Payment;
import com.example.personalassistant.entity.User;
import com.example.personalassistant.entity.UserLogin;
import com.example.personalassistant.repository.UserLoginRepository;
import com.example.personalassistant.repository.UserRepository;
import com.example.personalassistant.service.EmailService;
import com.example.personalassistant.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ForgotPasswordAndPaymentAuditTest {

    private UserService userService;
    private UserLogin testLogin;
    private User testUser;
    private String dispatchedPassword;

    @BeforeEach
    public void setup() {
        userService = new UserService();

        testLogin = new UserLogin();
        testLogin.setEmail("registered@gmail.com");
        testLogin.setPassword("$2a$10$hashedInitialPassword");
        testLogin.setAccountLocked(true);
        testLogin.setFailedLoginAttempts(4);

        testUser = new User();
        testUser.setEmail("registered@gmail.com");
        testUser.setName("Registered Traveler");

        // Mock UserLoginRepository
        UserLoginRepository loginRepo = (UserLoginRepository) Proxy.newProxyInstance(
                UserLoginRepository.class.getClassLoader(),
                new Class<?>[]{UserLoginRepository.class},
                (proxy, method, args) -> {
                    if ("findByEmail".equals(method.getName())) {
                        if ("registered@gmail.com".equals(args[0])) return Optional.of(testLogin);
                        return Optional.empty();
                    }
                    if ("save".equals(method.getName())) return args[0];
                    return null;
                }
        );

        // Mock UserRepository
        UserRepository userRepo = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    if ("findByEmail".equals(method.getName())) {
                        if ("registered@gmail.com".equals(args[0])) return Optional.of(testUser);
                        return Optional.empty();
                    }
                    if ("save".equals(method.getName())) return args[0];
                    return null;
                }
        );

        EmailService customEmailService = new EmailService() {
            @Override
            public void sendNewPassword(String toEmail, String newPassword) {
                dispatchedPassword = newPassword;
            }
        };

        PasswordEncoder encoder = new PasswordEncoder() {
            @Override
            public String encode(CharSequence raw) {
                return "ENCODED_" + raw;
            }

            @Override
            public boolean matches(CharSequence raw, String encoded) {
                return ("ENCODED_" + raw).equals(encoded);
            }
        };

        ReflectionTestUtils.setField(userService, "userLoginRepository", loginRepo);
        ReflectionTestUtils.setField(userService, "userRepository", userRepo);
        ReflectionTestUtils.setField(userService, "emailService", customEmailService);
        ReflectionTestUtils.setField(userService, "passwordEncoder", encoder);
    }

    @Test
    public void testForgotPasswordUnregisteredEmailReturnsIncorrectDetails() {
        ResponseEntity<?> res = userService.forgotPassword("notregistered@gmail.com");
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertNotNull(body);
        assertEquals("Incorrect Details", body.get("message"));
    }

    @Test
    public void testForgotPasswordRegisteredEmailDispatchesNewPasswordAndUnlocks() {
        ResponseEntity<?> res = userService.forgotPassword("registered@gmail.com");
        assertEquals(HttpStatus.OK, res.getStatusCode());

        assertNotNull(dispatchedPassword);
        assertTrue(dispatchedPassword.length() >= 8);
        assertFalse(testLogin.isAccountLocked());
        assertEquals(0, testLogin.getFailedLoginAttempts());
        assertEquals("ENCODED_" + dispatchedPassword, testLogin.getPassword());
    }

    @Test
    public void testPaymentAuditGuardPreventsDeletion() {
        Payment payment = new Payment();
        assertThrows(UnsupportedOperationException.class, payment::preventDeletion);
    }
}
