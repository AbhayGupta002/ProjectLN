package com.example.personalassistant.controller;

import com.example.personalassistant.dto.Response;
import com.example.personalassistant.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import com.example.personalassistant.service.*;
import org.springframework.web.bind.annotation.*;
import com.example.personalassistant.entity.Hotel;
import com.example.personalassistant.dto.UpdateHotelProfile;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@RequestMapping("/api/hotellogindashboard")
public class HotelDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserDashboardService userDashboardService;

    @Autowired
    private HotelDashboardService hotelDashboardService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Response> getDashboardStats(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        return hotelDashboardService.getDashboardStats(email);
    }


    @GetMapping("/hotelprofile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {

        // Remove "Bearer "
        String hotelToken = authHeader.replace("Bearer ", "").trim();

        String email = hotelService.extractEmailFromToken(hotelToken);

        Hotel hotel = hotelService.getHotelByEmail(email);

        return ResponseEntity.ok(hotel);
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateHotelProfile request) {

        String token = authHeader.replace("Bearer ", "");
        String email = hotelService.extractEmailFromToken(token);

        Hotel updatedUser = hotelDashboardService.updateProfile(email, request);

        return ResponseEntity.ok(updatedUser);
    }



    // Get complaints by USER
//    @GetMapping("/user/{userId}")
//    public List<Complaint> getUserComplaints(@PathVariable Long userId) {
//        return userDashboardService.getUserComplaints(userId);
//    }

}

