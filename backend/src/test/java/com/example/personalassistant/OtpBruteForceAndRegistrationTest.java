package com.example.personalassistant;

import com.example.personalassistant.dto.OtpVerificationResult;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.dto.UserDto;
import com.example.personalassistant.entity.OtpVerification;
import com.example.personalassistant.entity.User;
import com.example.personalassistant.entity.UserLogin;
import com.example.personalassistant.repository.OtpRepository;
import com.example.personalassistant.repository.UserLoginRepository;
import com.example.personalassistant.repository.UserRepository;
import com.example.personalassistant.security.JwtUtil;
import com.example.personalassistant.service.EmailService;
import com.example.personalassistant.service.OtpService;
import com.example.personalassistant.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class OtpBruteForceAndRegistrationTest {

    private OtpService otpService;
    private UserService userService;
    private OtpVerification activeOtpEntity;
    private UserLogin savedLogin;
    private User savedUser;

    @BeforeEach
    public void setup() {
        otpService = new OtpService();
        userService = new UserService();

        activeOtpEntity = new OtpVerification();
        activeOtpEntity.setId(1L);
        activeOtpEntity.setEmail("newtraveler@gmail.com");
        activeOtpEntity.setOtp("654321");
        activeOtpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        activeOtpEntity.setVerified(false);
        activeOtpEntity.setFailedAttempts(0);

        OtpRepository otpRepoProxy = (OtpRepository) Proxy.newProxyInstance(
                OtpRepository.class.getClassLoader(),
                new Class<?>[]{OtpRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findTopByEmailOrderByIdDesc".equals(name)) {
                        if (activeOtpEntity != null && args != null && args.length > 0
                                && activeOtpEntity.getEmail().equalsIgnoreCase((String) args[0])) {
                            return Optional.of(activeOtpEntity);
                        }
                        return Optional.empty();
                    }
                    if ("save".equals(name)) {
                        activeOtpEntity = (OtpVerification) args[0];
                        return activeOtpEntity;
                    }
                    if ("deleteByEmail".equals(name)) {
                        return null;
                    }
                    return null;
                }
        );

        UserLoginRepository loginRepoProxy = (UserLoginRepository) Proxy.newProxyInstance(
                UserLoginRepository.class.getClassLoader(),
                new Class<?>[]{UserLoginRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("existsByEmail".equals(name)) {
                        return false;
                    }
                    if ("findByEmail".equals(name)) {
                        return Optional.empty();
                    }
                    if ("save".equals(name)) {
                        savedLogin = (UserLogin) args[0];
                        return savedLogin;
                    }
                    return null;
                }
        );

        UserRepository userRepoProxy = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("existsByEmail".equals(name)) {
                        return false;
                    }
                    if ("findByEmail".equals(name) || "findByMobile".equals(name)) {
                        return Optional.empty();
                    }
                    if ("save".equals(name)) {
                        savedUser = (User) args[0];
                        savedUser.setId(99L);
                        return savedUser;
                    }
                    return null;
                }
        );

        EmailService dummyEmailService = new EmailService() {
            @Override
            public void sendOtp(String toEmail, String otp) {}
            @Override
            public void sendNewPassword(String toEmail, String newPassword) {}
        };

        PasswordEncoder dummyEncoder = new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return "HASHED_" + rawPassword;
            }
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return ("HASHED_" + rawPassword).equals(encodedPassword);
            }
        };

        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "mySecretKey1234567890123456789012345678901234567890");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);

        ReflectionTestUtils.setField(otpService, "otpRepository", otpRepoProxy);
        ReflectionTestUtils.setField(otpService, "emailService", dummyEmailService);

        ReflectionTestUtils.setField(userService, "otpService", otpService);
        ReflectionTestUtils.setField(userService, "otpRepository", otpRepoProxy);
        ReflectionTestUtils.setField(userService, "userLoginRepository", loginRepoProxy);
        ReflectionTestUtils.setField(userService, "userRepository", userRepoProxy);
        ReflectionTestUtils.setField(userService, "emailService", dummyEmailService);
        ReflectionTestUtils.setField(userService, "passwordEncoder", dummyEncoder);
        ReflectionTestUtils.setField(userService, "jwtUtil", jwtUtil);
    }

    @Test
    public void testOtpIncorrectAttemptsTrackingAndLockoutOnThirdAttempt() {
        // Attempt 1 with wrong OTP
        OtpVerificationResult res1 = otpService.verifyOtpWithResult("newtraveler@gmail.com", "000001");
        assertFalse(res1.isSuccess());
        assertEquals(2, res1.getRemainingAttempts());
        assertFalse(res1.isLocked());
        assertTrue(res1.getMessage().contains("2 attempt(s) remaining"));

        // Attempt 2 with wrong OTP
        OtpVerificationResult res2 = otpService.verifyOtpWithResult("newtraveler@gmail.com", "000002");
        assertFalse(res2.isSuccess());
        assertEquals(1, res2.getRemainingAttempts());
        assertFalse(res2.isLocked());
        assertTrue(res2.getMessage().contains("1 attempt(s) remaining"));

        // Attempt 3 with wrong OTP -> Brute force threshold reached
        OtpVerificationResult res3 = otpService.verifyOtpWithResult("newtraveler@gmail.com", "000003");
        assertFalse(res3.isSuccess());
        assertEquals(0, res3.getRemainingAttempts());
        assertTrue(res3.isLocked());
        assertTrue(res3.getMessage().contains("Maximum attempts (3/3) exceeded"));

        // Attempt 4 even with correct OTP should be blocked because OTP is now locked/expired
        OtpVerificationResult res4 = otpService.verifyOtpWithResult("newtraveler@gmail.com", "654321");
        assertFalse(res4.isSuccess());
        assertTrue(res4.getMessage().contains("expired") || res4.getMessage().contains("exceeded"));
    }

    @Test
    public void testRegisterUserWithCorrectOtpSucceedsAndPersistsAccount() {
        UserDto dto = new UserDto();
        dto.setName("New Traveler");
        dto.setEmail("newtraveler@gmail.com");
        dto.setMobile("9876543210");
        dto.setPassword("SecretPass123");
        dto.setOtp("654321");

        ResponseEntity<Response> response = userService.registerUser(dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Boolean.TRUE, response.getBody().getSuccess());
        assertEquals("register successful", response.getBody().getMessage());

        assertNotNull(savedUser);
        assertEquals("newtraveler@gmail.com", savedUser.getEmail());
        assertEquals("New Traveler", savedUser.getName());

        assertNotNull(savedLogin);
        assertEquals("newtraveler@gmail.com", savedLogin.getEmail());
        assertEquals("HASHED_SecretPass123", savedLogin.getPassword());
    }

    @Test
    public void testRegisterUserWithoutOtpIsRejected() {
        UserDto dto = new UserDto();
        dto.setName("New Traveler");
        dto.setEmail("newtraveler@gmail.com");
        dto.setMobile("9876543210");
        dto.setPassword("SecretPass123");
        dto.setOtp(null);

        ResponseEntity<Response> response = userService.registerUser(dto);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotEquals(Boolean.TRUE, response.getBody().getSuccess());
        assertTrue(response.getBody().getMessage().contains("OTP code is required"));
    }
}
