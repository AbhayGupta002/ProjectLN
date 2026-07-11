package com.example.personalassistant.controller;

import com.example.personalassistant.dto.Response;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.personalassistant.entity.TourBooking;
import com.example.personalassistant.dto.TourBookingRequest;
import com.example.personalassistant.service.TourBookingService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.stream.Location;
import java.util.List;


@RestController
@RequestMapping("/api/tour-booking")
//@CrossOrigin("*")
public class TourBookingController {

    @Autowired
    private TourBookingService tourBookingService;

    @PostMapping("/create")
    public ResponseEntity<?> tourBookByUser(@RequestBody TourBookingRequest request) {
        return tourBookingService.bookTourByUser(request);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBookings(@PathVariable Long userId) {
        return tourBookingService.getBookingsByUser(userId);
    }

    @PutMapping("/confirm/{bookingId}")
    public TourBooking confirmBookingMessage(  ///send message if booking is confirmed Update it
            @PathVariable Long bookingId) {
            return tourBookingService.confirmBooking(bookingId);
    }

    @PutMapping("/cancle-booking")
    public ResponseEntity<?> cancleTourBookingByUser(@PathVariable Long bookingId , @PathVariable Long userId){
        return tourBookingService.cancelTourBooking(bookingId, userId);
    }

    @PutMapping("/reject/{bookingId}")
    public TourBooking rejectBooking(
            @PathVariable Long bookingId) {
        return tourBookingService.rejectBooking(bookingId);
    }

    // 🔥 GET ALL PENDING BOOKINGS this will only in admin controller
//    @GetMapping("/pending")
//    public List<TourBooking> getPendingBookings() {
//        return tourBookingService.getPendingBookings();
//    }

    // 🔥 GET PENDING BOOKINGS FOR A HOTEL
    @GetMapping("/pending/hotel/{hotelId}")
    public ResponseEntity<?> getPendingBookingsByHotel(
            @PathVariable Long hotelId) {
        return tourBookingService.getPendingBookingsByHotel(hotelId);
    }

    @GetMapping("/tours")
    public ResponseEntity<Response> getAllTours(){
        return tourBookingService.getAllTours();
    }

//    @GetMapping("/tourbylocation")
//    public ResponseEntity<Response> searchTours(@RequestParam String location, @RequestParam(defaultValue = "asc") String sort) {
//        return tourBookingService.searchTours(location, sort);
//    }
}
