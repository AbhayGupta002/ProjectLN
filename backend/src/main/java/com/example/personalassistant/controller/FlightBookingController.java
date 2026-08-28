package com.example.personalassistant.controller;

import com.example.personalassistant.dto.FlightBookingRequest;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flight-bookings")
public class FlightBookingController {

    @Autowired
    private FlightService flightService;

    @PostMapping("/book")
    public ResponseEntity<Response> bookFlight(@RequestBody FlightBookingRequest request) {
        return flightService.bookFlight(request);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Response> getUserFlightBookings(@PathVariable Long userId) {
        return flightService.getUserBookings(userId);
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<Response> cancelFlightBooking(@PathVariable Long id) {
        return flightService.cancelFlightBooking(id);
    }
}
