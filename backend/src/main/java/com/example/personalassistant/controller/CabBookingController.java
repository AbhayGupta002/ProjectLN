package com.example.personalassistant.controller;

import com.example.personalassistant.dto.CabBookingRequest;
import com.example.personalassistant.entity.CabBooking;
import com.example.personalassistant.service.CabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cab-bookings")
public class CabBookingController {

    @Autowired
    private CabService cabService;

    @PostMapping("/book")
    public ResponseEntity<?> bookCab(@RequestBody CabBookingRequest request, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body("User must be authenticated");
        }
        try {
            CabBooking booking = cabService.bookCab(request, authentication.getName());
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancelCabBooking(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body("User must be authenticated");
        }
        try {
            CabBooking booking = cabService.cancelCabBooking(id, authentication.getName());
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<CabBooking>> getMyCabBookings(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cabService.getUserCabBookings(authentication.getName()));
    }
}
