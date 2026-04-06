package com.example.personalassistant.controller;

import com.example.personalassistant.dto.*;
import com.example.personalassistant.entity.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.personalassistant.service.OtpService;
import com.example.personalassistant.service.UserService;
import com.example.personalassistant.service.AdminService;
import com.example.personalassistant.service.HotelService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")   // ✅ SAME BASE PATH
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private OtpService otpService;

    @Autowired
    OtpController otpController;

    @PostMapping("/login")   // /api/auth/login
    @Qualifier("userDetailsService")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return userService.loginUser(req.getEmail(), req.getPassword());
    }

//    @PostMapping("/login")   // /api/auth/login wo
//    @Qualifier("userDetailsService")
//    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse response) {
//        User serviceResponse =  userService.loginUser(req.getEmail(), req.getPassword());
//        String token = (String) ((Map<? , ?>)serviceResponse.getBody()).get("data");
//        if (token != null){
//            Cookie cookie = new Cookie("token", token);
//            cookie.setHttpOnly(true); // 🔐 important
//            cookie.setSecure(false);  // true in production (HTTPS)
//            cookie.setPath("/");
//            cookie.setMaxAge(24 * 60 * 60);
//
//            response.addCookie(cookie);
//        }
//        return ResponseEntity.ok("Login successfu");
//    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDto userDto) {
//        otpService.sendOtp(userDto.getEmail());
        return userService.registerUser(userDto);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordReqDto req) {
        return userService.forgotPassword(req.getEmail());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordReqDto req) {
        return userService.resetPassword(req.getToken(), req.getNewPassword());
    }

    @PostMapping("/hotellogin")
//    @Qualifier("hotelDetailsService")
    public ResponseEntity<?> hotelLogin(@RequestBody HotelLoginRequest loginRequest) {
        return hotelService.loginHotel(loginRequest.getEmail(), loginRequest.getPassword());
    }

    @PostMapping("/hotelregister")
    public ResponseEntity<?> hotelRegister(@RequestBody HotelDto hotelDto) {
        return hotelService.registerHotel(hotelDto);
    }



}
