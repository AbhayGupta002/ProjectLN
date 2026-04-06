package com.example.personalassistant.controller;

import com.example.personalassistant.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.personalassistant.dto.AdminDto;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.dto.AdminLoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TourBookingService tourBookingService;

    @Autowired
    private HotelDashboardService hotelDashboardService;

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private MongoService mongoService;

    // -------- AUTH --------
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody AdminLoginRequest request) {
        return adminService.adminLogin(request.getEmail(), request.getPassword());
    }

    @PostMapping("/register")
    public ResponseEntity<Response> registerAdmin(@RequestBody AdminDto adminDto) {
        return adminService.adminRegister(adminDto);
    }

    // -------- HOTELS --------
    @GetMapping("/hotels")
    public ResponseEntity<?> getAllHotels(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        // You can pass this token to your service if needed
        return adminDashboardService.getAllHotels();
    }

    @GetMapping("/hotels/active")
    public ResponseEntity<?> getActiveHotels(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        return adminDashboardService.getActiveHotels();
    }

    @GetMapping("/hotels/inactive")
    public ResponseEntity<?> getInactiveHotels(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        return adminDashboardService.getInactiveHotels();
    }

    // -------- USERS --------
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        return adminDashboardService.getActiveUsers();
    }

    // -------- ACTIONS --------
    @PatchMapping("/suspend-hotel/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> suspendHotel(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        String token = extractToken(authHeader);
        return adminDashboardService.suspendHotelAccount(id);
    }

    @PatchMapping("/suspend-user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> suspendUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        String token = extractToken(authHeader);
        return adminDashboardService.suspendUserAccount(id);
    }

    // -------- BOOKINGS --------
    @GetMapping("/bookings/pending")
    public ResponseEntity<?> getPendingBookings(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        return ResponseEntity.ok(tourBookingService.getPendingBookings());
    }

    // -------- SEARCH --------
    @GetMapping("/search")
    public ResponseEntity<?> searchHotel(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String location) {
        String token = extractToken(authHeader);
        return hotelDashboardService.searchTourByLocation(location);
    }

    // -------- HELPER METHOD --------
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.replace("Bearer ", "");
        }
        throw new RuntimeException("Authorization token missing or invalid");
    }

    @GetMapping("/prompts")
    public ResponseEntity<?> getUserPrompts() {
        return ResponseEntity.ok(mongoService.getUserPrompt());
    }
}

