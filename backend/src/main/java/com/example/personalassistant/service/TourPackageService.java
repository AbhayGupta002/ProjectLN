package com.example.personalassistant.service;

import java.util.List;
import org.slf4j.Logger;
import java.util.Optional;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.personalassistant.entity.Hotel;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.enums.TourEnum;
import com.example.personalassistant.dto.ErrorDetails;
import com.example.personalassistant.entity.HotelLogin;
import com.example.personalassistant.dto.TourPackageDTO;
import com.example.personalassistant.entity.TourPackage;
import com.example.personalassistant.repository.HotelRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.personalassistant.repository.HotelLoginRepository;
import com.example.personalassistant.repository.TourPackageRepository;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;



@Service
public class TourPackageService {

    private static final Logger log = LoggerFactory.getLogger(TourPackageService.class);
    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HotelLoginRepository hotelLoginRepository;

    @Autowired
    private TourPackageRepository tourPackageRepository;

    // Create a new tour package
    @CacheEvict(value = {"toursCache", "dashboardCache"}, allEntries = true)
    public ResponseEntity<Response> createPackage(String email, TourPackageDTO dto) {

        Response response = new Response();

        // 🔐 SAFETY CHECK
        if (email == null || email.isEmpty()) {
            response.setError(new ErrorDetails(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid token: email missing"
            ));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 1️⃣ Get hotel using TOKEN email (NOT DTO)
        Optional<Hotel> hotelOpt = hotelRepository.findByEmail(email);

        if (!hotelOpt.isPresent()) {
            ErrorDetails errorDetails = new ErrorDetails(
                    HttpStatus.BAD_REQUEST,
                    "Hotel not found for this user"
            );
            response.setError(errorDetails);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Hotel hotel = hotelOpt.get();

        // 2️⃣ Check duplicate title for same hotel
        boolean exists = tourPackageRepository.existsByEmailAndTitle(email, dto.getTitle());

        if (exists) {
            ErrorDetails errorDetails = new ErrorDetails(
                    HttpStatus.ALREADY_REPORTED,
                    "Tour already exists with this title"
            );
            response.setError(errorDetails);
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(response);
        }

        // 3️⃣ Create Tour
        TourPackage tourPackage = new TourPackage();
        tourPackage.setTitle(dto.getTitle());
        tourPackage.setPrice(dto.getPrice());
        tourPackage.setEmail(email); // ✅ IMPORTANT
        tourPackage.setHotel(hotel); // ✅ LINK HOTEL
        tourPackage.setLocation(dto.getLocation());
        tourPackage.setDescription(dto.getDescription());
        tourPackage.setDurationDays(dto.getDurationDays());
        tourPackage.setImageUrl(dto.getImageUrl());

        // ✅ FIX ENUM ISSUE
        tourPackage.setTourStatus(TourEnum.PENDING);

        // 4️⃣ Save
        TourPackage saved = tourPackageRepository.save(tourPackage);

        // 5️⃣ Response DTO
        TourPackageDTO resDto = new TourPackageDTO();
        resDto.setId(saved.getId());
        resDto.setTitle(saved.getTitle());
        resDto.setDescription(saved.getDescription());
        resDto.setDurationDays(saved.getDurationDays());
        resDto.setLocation(saved.getLocation());
        resDto.setImageUrl(saved.getImageUrl());
        resDto.setPrice(saved.getPrice());

        response.setData(resDto);

        return ResponseEntity.ok(response);
    }

    // Get all tour packages
    public ResponseEntity<Response> getAllPackages(String email) {

        Response response = new Response();

        List<TourPackage> packages = tourPackageRepository
                .findByEmail(email);

        if (packages.isEmpty()) {
            ErrorDetails errorDetails = new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "No tour packages found for email: " + email
            );

            response.setError(errorDetails);
            return ResponseEntity.ok(response);
        }

        response.setData(packages);
        return ResponseEntity.ok(response);
    }

    // Get package by ID
    public TourPackage getPackageById(Long id) {
        return tourPackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour Package Not Found: " + id));
    }

    // Update package
    @CacheEvict(value = {"toursCache", "dashboardCache"}, allEntries = true)
    public TourPackage updatePackage(Long id, TourPackage updatedPackage) {
        TourPackage existing = getPackageById(id);

        existing.setTitle(updatedPackage.getTitle());
        existing.setDescription(updatedPackage.getDescription());
        //existing.setLocation(updatedPackage.getLocation());
        existing.setDurationDays(updatedPackage.getDurationDays());
        existing.setPrice(updatedPackage.getPrice());
        //existing.setImageUrl(updatedPackage.getImageUrl());

        return tourPackageRepository.save(existing);
    }

    // Delete package
    @CacheEvict(value = {"toursCache", "dashboardCache"}, allEntries = true)
    public ResponseEntity<Response> deletePackage(Long id, String email, String password) {

        Response response = new Response();

        // Step 1: Check Hotel Exists
        HotelLogin hotel = hotelLoginRepository.findByEmail(email).orElse(null);

        if (hotel == null) {
            ErrorDetails error = new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "Hotel does not exist"
            );
            response.setError(error);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Step 2: Validate Password
        if (!passwordEncoder.matches(password, hotel.getPassword())) {
            ErrorDetails error = new ErrorDetails(
                    HttpStatus.BAD_REQUEST,
                    "Incorrect password!"
            );
            response.setError(error);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Step 3: Check Package Exists
        Optional<TourPackage> optionalPackage = tourPackageRepository.findById(id);

        if (!optionalPackage.isPresent()) {
            ErrorDetails error = new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "Tour package does not exist"
            );
            response.setError(error);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        TourPackage tourPackage = optionalPackage.get();

        // Step 4: Check Ownership (VERY IMPORTANT 🔥)
//        if (!tourPackage.getHotelLogin().getId().equals(hotel.getId())) {
//            ErrorDetails error = new ErrorDetails(
//                    HttpStatus.FORBIDDEN,
//                    "You are not authorized to delete this package"
//            );
//            response.setError(error);
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//        }

        // Step 5: Soft Delete (Update Status)
        tourPackage.setTourStatus(TourEnum.CANCELLED);
        tourPackageRepository.save(tourPackage);

        // Step 6: Convert to DTO
        TourPackageDTO dto = new TourPackageDTO();
        dto.setId(tourPackage.getId());
        dto.setTitle(tourPackage.getTitle());
        dto.setTourStatus(tourPackage.getTourStatus());

        response.setData(dto);
        return ResponseEntity.ok(response);
    }
}
