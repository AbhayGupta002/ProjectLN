package com.example.personalassistant.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.personalassistant.entity.Hotel;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.dto.ErrorDetails;
import com.example.personalassistant.enums.AccountEnum;
import com.example.personalassistant.entity.TourPackage;
import com.example.personalassistant.repository.HotelRepository;
import com.example.personalassistant.dto.HotelTourFiltterRequest;
import com.example.personalassistant.repository.TourPackageRepository;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class PublicService {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private TourPackageRepository tourBookingRepository;

    @Autowired
    private TourPackageRepository tourPackageRepository;

    @Cacheable(value = "toursCache", key = "#location.trim().toLowerCase() + '-' + #sortDirection")
    public ResponseEntity<Response> getToursByLocation(String location, String sortDirection) {

        Response response = new Response();

        if (location == null || location.trim().isEmpty()) {
            response.setError(new ErrorDetails(HttpStatus.BAD_REQUEST, "Location required"));
            return ResponseEntity.badRequest().body(response);
        }

        List<TourPackage> tours =
                tourPackageRepository.findByLocationFlexibleSorted(
                        location.trim(),
                        sortDirection
                );

        response.setData(tours);
        return ResponseEntity.ok(response);
    }

    @Cacheable(value = "toursCache", key = "'days-' + #days")
    public ResponseEntity<Response> getToursByDays(int days) {
        Response response = new Response();

        if (days <= 0) {
            response.setError(new ErrorDetails(HttpStatus.BAD_REQUEST, "Invalid days"));
            return ResponseEntity.badRequest().body(response);
        }

        // Fetch tours using durationDays
        List<TourPackage> tours = tourPackageRepository.findByDurationRangeSorted(days - 2, days + 2, days);
        response.setData(tours);

        return ResponseEntity.ok(response);
    }


    public ResponseEntity<Response> filterHotels(HotelTourFiltterRequest request) {

        Response response = new Response();

        List<Hotel> hotels;

        try {

            // 👉 If NO filter at all → return all ACTIVE hotels
            if (request.getUserLat() == null &&
                    request.getUserLng() == null &&
                    request.getRangeKm() == null &&
                    request.getCity() == null &&
                    request.getStatus() == null) {

                hotels = hotelRepository.findByAccountEnum(AccountEnum.ACTIVE);

            } else {

                // 👉 Call filter query
                hotels = hotelRepository.findFilteredHotels(
                        request.getUserLat(),
                        request.getUserLng(),
                        request.getRangeKm(),
                        request.getCity()
                );
            }

            // ❌ If empty result
            if (hotels == null || hotels.isEmpty()) {
                ErrorDetails errorDetails = new ErrorDetails(
                        HttpStatus.NO_CONTENT,
                        "No hotels found with given filters"
                );

                response.setError(errorDetails);

                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
            }

            // ✅ Success response
            response.setData(hotels);
            return ResponseEntity.ok(response);

        } catch (Exception e) {

            ErrorDetails errorDetails = new ErrorDetails(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );

            response.setError(errorDetails);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public ResponseEntity<Response> getTopHotels() {

        Response response = new Response();
        try {
            List<Hotel> hotels = hotelRepository.findTopHotels();

            if (hotels == null || hotels.isEmpty()) {
                ErrorDetails error = new ErrorDetails(
                        HttpStatus.NO_CONTENT,
                        "No top hotels found"
                );

                response.setError(error);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
            }

            response.setData(hotels);
            return ResponseEntity.ok(response);

        } catch (Exception e) {

            ErrorDetails error = new ErrorDetails(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );

            response.setError(error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}