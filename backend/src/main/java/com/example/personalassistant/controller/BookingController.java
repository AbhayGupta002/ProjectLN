package com.example.personalassistant.controller;

import com.example.personalassistant.dto.BookingRequest;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.security.JwtUtil;
import com.example.personalassistant.service.BookingService;
import com.example.personalassistant.service.HotelDashboardService;
import com.example.personalassistant.service.TourBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TourBookingService tourBookingService;

    @Autowired
    private HotelDashboardService hotelDashboardService;

//    public BookingController(BookingService bookingService, JwtUtil jwtUtil) {
//        this.bookingService = bookingService;
//        this.jwtUtil = jwtUtil;
//    }

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

//    @PostConstruct
//    public void testRedis() {
//        redisTemplate.opsForValue().set("test", "working");
//        System.out.println(redisTemplate.opsForValue().get("test"));
//    }

    @PostMapping("/bookhotel")
    public ResponseEntity<Response> bookHotel(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody BookingRequest bookingRequest) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        return bookingService.bookHotel(email, bookingRequest.getHotelId());
    }

    @GetMapping("/getallbookingbyhotel")
    public ResponseEntity<Response> getAllBooking(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
//        String email = jwtUtil.extractRole(token);
        String email = jwtUtil.extractUsername(token);
        return bookingService.getAllBookingsByHotel(email);
    }

    @GetMapping("/gettourbooking/{userId}")
    public ResponseEntity<?> getTourBookings(@PathVariable Long userId) {
        return tourBookingService.getBookingsByUser(userId);
    }

//    @GetMapping("/getuserbookings")
//    public ResponseEntity<?> getUserBookings(@RequestBody BookingRequest bookingRequest){
//        return bookingService.getUserBookings(bookingRequest.getEmail());
//
//    }

    @GetMapping("/getuserbookings")
    public ResponseEntity<?> getUserBookings(@RequestParam String email){
        return bookingService.getUserBookings(email);
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        return bookingService.cancelBooking(id);
    }
}
