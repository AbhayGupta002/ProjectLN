//package com.example.personalassistant.controller;
//
//import java.util.List;
//
//import com.example.personalassistant.dto.RatingSummaryDTO;
//import com.example.personalassistant.entity.HotelRating;
//import com.example.personalassistant.repository.HotelsRatingsService;
//import com.example.personalassistant.service.HotelRatingService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import com.example.personalassistant.dto.HotelRatingDto;
//
//
//@RestController
//@RequestMapping("/api/ratings")
//public class HotelRatingController {
//
//    @Autowired
//    private HotelRating hotelRatingService;
//
//    public HotelRatingController(HotelRatingService hotelRatingService) {
//        this.hotelRatingService = hotelRatingService;
//    }
//
//    // ✅ Submit Rating
//    @PostMapping
//    public ResponseEntity<HotelRatingDto> submitRating(
//            @RequestBody HotelRatingDto request,
//            @RequestHeader("Authorization") String token) {
//
//        return hotelRatingService.submitRating(request, token);
//    }
//
//    // ✅ Get All Ratings for Hotel
//    @GetMapping("/hotel/{hotelId}")
//    public ResponseEntity<List<HotelRatingDto>> getRatingsByHotel(
//            @PathVariable Long hotelId) {
//
//        return hotelRatingService.getRatingsByHotel(hotelId);
//    }
//
//    // ✅ Get Rating Summary
//    @GetMapping("/hotel/{hotelId}/summary")
//    public ResponseEntity<RatingSummaryDTO> getRatingSummary(
//            @PathVariable Long hotelId) {
//
//        return hotelRatingService.getRatingSummary(hotelId);
//    }
//}
