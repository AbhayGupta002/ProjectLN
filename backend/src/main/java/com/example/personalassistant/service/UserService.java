package com.example.personalassistant.service;

import java.util.UUID;
import java.time.LocalDateTime;

import com.example.personalassistant.entity.OtpVerification;
import com.example.personalassistant.enums.AccountEnum;
import com.example.personalassistant.repository.OtpRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.personalassistant.entity.User;
import com.example.personalassistant.dto.UserDto;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.security.JwtUtil;
import com.example.personalassistant.dto.ErrorDetails;
import com.example.personalassistant.entity.UserLogin;
import com.example.personalassistant.dto.ChangePasswordDto;
import com.example.personalassistant.repository.UserRepository;
import com.example.personalassistant.repository.UserLoginRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;



@Slf4j
@Service
//@Transactional  WIP
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserLoginRepository userLoginRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private OtpService otpService;

    public ResponseEntity<Response> registerUser(UserDto dto) {

        Response response = new Response();

        if (userLoginRepository.existsByEmail(dto.getEmail())) {
            ErrorDetails error = new ErrorDetails(HttpStatus
                            .BAD_REQUEST,
                    "User already exists"
            );
            response.setError(error);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (!dto.getEmail().endsWith("@gmail.com")) {
            ErrorDetails error = new ErrorDetails(
                    HttpStatus.BAD_REQUEST,
                    "Only Gmail addresses are allowed!"
            );
            response.setError(error);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
//        // 2️⃣ Check if email is verified
//        OtpVerification otp = otpRepository.findByEmail(dto.getEmail()).orElse(null);
//        if (otp == null || !otp.isVerified()) {
//            response.setError(new ErrorDetails(
//                    HttpStatus.BAD_REQUEST,
//                    "Email not verified. Please verify OTP first."
//            ));
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        }
//
//        // 3️⃣ Optional: Check OTP expiry again (extra safety)
//        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
//            response.setError(new ErrorDetails(
//                    HttpStatus.BAD_REQUEST,
//                    "OTP expired. Please request new OTP."
//            ));
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        }
            User user = new User();
            user.setName(dto.getName());
            user.setEmail(dto.getEmail());
            user.setMobile(dto.getMobile());
            user.setAccountEnum(AccountEnum.ACTIVE);
            User savedUser = userRepository.save(user);

            UserLogin login = new UserLogin();  //WIP
            login.setEmail(dto.getEmail());
            login.setPassword(passwordEncoder.encode(dto.getPassword()));
            login.setEmailVerified(true);

            userLoginRepository.save(login);

//            otpRepository.delete(otp);

            response.setData(savedUser);
            return ResponseEntity.ok(response);
    }


    public ResponseEntity<Response> loginUser(String email, String password) {

    Response response = new Response();
    UserLogin user = userLoginRepository.findByEmail(email).orElse(null);  // in optional null doesn't work
    if (user == null) {                 /*in Optional<> null doesn't support....  */
        ErrorDetails error = new ErrorDetails(
                HttpStatus.NOT_FOUND,
                "User does not exist"
        );
        response.setError(error);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    if (!passwordEncoder.matches(password, user.getPassword())) {
        ErrorDetails error = new ErrorDetails(
                HttpStatus.BAD_REQUEST,
                "Invalid email or password"
        );
        response.setError(error);
        return ResponseEntity.badRequest().body(response);
    }

    String token = jwtUtil.generateToken(email,"User");

    response.setData(token);
    return ResponseEntity.ok(response);
}

    public ResponseEntity<?> forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("enter mail id:");
        }

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(15)); // 15-min expiration
        userRepository.save(user);

        return ResponseEntity.ok("Reset token generated: " + token);
    }

    public ResponseEntity<?> resetPassword(String token, String newPassword) {

        User user = userRepository.findByResetToken(token);

        if (user == null) {
            return ResponseEntity.status(400).body("Invalid token");
        }

        if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(400).body("Token expired");
        }
        UserLogin userLogin = new UserLogin();

        userLogin.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setTokenExpiry(null);

        userRepository.save(user);
        return ResponseEntity.ok("Password reset successful!");
    }

    public ResponseEntity<Response> changePassword(String email, ChangePasswordDto dto) {

        UserLogin login = userLoginRepository.findById(email).orElse(null);
        if (login == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new Response(
                            null,
                            new ErrorDetails(HttpStatus.NOT_FOUND, "Account not found")
                    ));
        }

        // Check current password
        if (!passwordEncoder.matches(dto.getCurrentPassword(), login.getPassword())) {
            return ResponseEntity
                    .badRequest()
                    .body(new Response(
                            null,
                            new ErrorDetails(HttpStatus.BAD_REQUEST, "Incorrect current password")
                    ));
        }

        // Check new passwords match
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return ResponseEntity
                    .badRequest()
                    .body(new Response(
                            null,
                            new ErrorDetails(HttpStatus.BAD_REQUEST, "New passwords do not match")
                    ));
        }

        // Save new password
        login.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userLoginRepository.save(login);

        return ResponseEntity.ok(
                new Response(
                        "Password changed successfully",
                        null
                )
        );
    }


    public String extractEmailFromToken(String token) {
        token = token.replace("Bearer ", "");
        return jwtUtil.extractUsername(token);
    }

    public User getUserByEmail(String email) {
        Response response = new Response();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


}
