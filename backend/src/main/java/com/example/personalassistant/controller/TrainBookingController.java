package com.example.personalassistant.controller;

import com.example.personalassistant.dto.TrainBookingRequest;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.service.TrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/train-bookings")
public class TrainBookingController {

    @Autowired
    private TrainService trainService;

    @PostMapping("/book")
    public ResponseEntity<Response> bookTrain(@RequestBody TrainBookingRequest request) {
        return trainService.bookTrain(request);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Response> getUserTrainBookings(@PathVariable Long userId) {
        return trainService.getUserBookings(userId);
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<Response> cancelTrainBooking(@PathVariable Long id) {
        return trainService.cancelTrainBooking(id);
    }
}
