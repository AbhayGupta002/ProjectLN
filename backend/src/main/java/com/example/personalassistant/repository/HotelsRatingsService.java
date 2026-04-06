package com.example.personalassistant.repository;

import com.example.personalassistant.dto.HotelRatingDto;
import com.example.personalassistant.dto.RatingSummaryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;


public interface HotelsRatingsService {

    ResponseEntity<HotelRatingDto> submitRating(
            HotelRatingDto request,
            String token);

    ResponseEntity<List<HotelRatingDto>> getRatingsByHotel(
            Long hotelId);

    ResponseEntity<RatingSummaryDTO> getRatingSummary(
            Long hotelId);
}
