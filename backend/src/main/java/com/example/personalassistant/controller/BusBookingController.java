package com.example.personalassistant.controller;

import com.example.personalassistant.dto.BusBookingRequest;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.service.BusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bus-bookings")
public class BusBookingController {

    @Autowired
    private BusService busService;

    @PostMapping("/book")
    public ResponseEntity<Response> bookBus(@RequestBody BusBookingRequest request) {
        return busService.bookBus(request);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Response> getUserBusBookings(@PathVariable Long userId) {
        return busService.getUserBookings(userId);
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<Response> cancelBusBooking(@PathVariable Long id) {
        return busService.cancelBusBooking(id);
    }
}
