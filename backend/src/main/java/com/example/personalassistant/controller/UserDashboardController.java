package com.example.personalassistant.controller;

import com.example.personalassistant.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.personalassistant.entity.User;
import com.example.personalassistant.entity.Complaint;
import org.springframework.security.core.Authentication;
import com.example.personalassistant.service.UserService;
import com.example.personalassistant.service.BookingService;
import com.example.personalassistant.service.UserDashboardService;
import com.example.personalassistant.service.HotelDashboardService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class UserDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserDashboardService userDashboardService;

    @Autowired
    private HotelDashboardService hotelDashboardService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {

        // Remove "Bearer "
        String token = authHeader.replace("Bearer ", "").trim();

        String email = userService.extractEmailFromToken(token);

        User user = userService.getUserByEmail(email);

        return ResponseEntity.ok(user);
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateProfileRequest request) {

        String token = authHeader.replace("Bearer ", "");
        String email = userService.extractEmailFromToken(token);

        User updatedUser = userDashboardService.updateProfile(email, request);

        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Response> changePassword(
            @RequestBody ChangePasswordDto dto,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return userService.changePassword(email, dto);
    }


    @PostMapping("/add")
    public Complaint addComplaint(@RequestBody ComplaintRequest request) {
        return userDashboardService.addComplaint(request);
    }

    // Get complaints by USER
    @GetMapping("/user/{userId}")
    public List<Complaint> getUserComplaints(@PathVariable Long userId) {
        return userDashboardService.getUserComplaints(userId);
    }

    @GetMapping("/get-active-hotel")
    public ResponseEntity<?> getHotels(){
        return hotelDashboardService.getAllActiveHotels();
    }

    @PatchMapping("/disable-account")
    public ResponseEntity<?> disableAccount(
            @RequestBody DeleteAccountDto deleteAccount) {
        return userDashboardService.disableAccount(
                deleteAccount.getEmail(),
                deleteAccount.getPassword()
        );
    }
    @GetMapping("/search")
    public ResponseEntity<?> searchHotel(@RequestParam String location) {
        return hotelDashboardService.searchTourByLocation(location);
    }


//    record DashboardStats(int totalBookings, int totalHotels) { }
}
