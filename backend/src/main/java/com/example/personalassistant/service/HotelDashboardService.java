package com.example.personalassistant.service;

import com.example.personalassistant.entity.*;
import com.example.personalassistant.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.personalassistant.dto.HotelDto;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.security.JwtUtil;
import com.example.personalassistant.dto.ErrorDetails;
import com.example.personalassistant.enums.AccountEnum;
import com.example.personalassistant.dto.UpdateHotelProfile;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class HotelDashboardService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserLoginRepository userLoginRepository;

    @Autowired
    private HotelLoginRepository hotelLoginRepository;

    @Autowired
    private TourBookingRepository tourBookingRepository;

    @Autowired
    private TourPackageRepository tourPackageRepository;

    @CacheEvict(value = "toursCache", allEntries = true)
    public ResponseEntity<Response> getDashboardStats(String email) {

        Response response = new Response();

        Hotel hotel = hotelRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        int totalTours = tourPackageRepository.countByHotel(hotel);

        List<Booking> bookings = bookingRepository.findByHotel(hotel);
        int totalBookings = bookings.size();

        List<TourBooking> tourBookings = tourBookingRepository.findByEmail(email);
        int totalTourBookings = tourBookings.size();

        double revenue = bookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getBookingStatus()))
                .mapToDouble(b -> Double.parseDouble(hotel.getPrice())) // or b.getAmount()
                .sum();

        long pending = bookings.stream()
                .filter(b -> "PENDING".equals(b.getBookingStatus()))
                .count();

        Map<String, Object> data = new HashMap<>();
        data.put("totalTours", totalTours);
        data.put("totalBookings", totalBookings);
        data.put("revenue", revenue);
        data.put("pending", pending);

        response.setData(data);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Response> getAllActiveHotels() {

    Response response = new Response();
    List<Hotel> hotels =
            hotelRepository.findByAccountEnum(AccountEnum.ACTIVE);
    if (hotels.isEmpty()) {
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus
                .NOT_FOUND
        ,"No active hotels found");
        response.setError(errorDetails);
        return ResponseEntity.ok(response);
    }
    response.setData(hotels);
    return ResponseEntity.ok(response);
}

    public ResponseEntity<?> disableAccount(String email, String password) {

        Optional<HotelLogin> loginOptional =
                hotelLoginRepository.findByEmail(email);
        if (loginOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Hotel not found");
        }

        HotelLogin login = loginOptional.get();

        // Check password
        if (!passwordEncoder.matches(password, login.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid password");
        }

        // Disable login
        Hotel hotel = new Hotel();
        hotel.setAccountEnum(AccountEnum.INACTIVE);
        hotelRepository.save(hotel);

        return ResponseEntity.ok("Account disabled successfully");
    }

    public Hotel getHotelProfile(String hotelToken) {
        String email = jwtUtil.extractUsername(hotelToken);

        Hotel hotel = hotelRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return hotel;
    }

    public ResponseEntity<?> searchTourByLocation(String location){

        List<TourBooking> existingTour = tourBookingRepository.findByLocationIgnoreCase(location);
        Response response = new Response();
        if(existingTour.isEmpty()){
            ErrorDetails error = new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "Tour Not found with this specific location"
            );
            response.setError(error);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        response.setData(existingTour);
        return ResponseEntity.ok(response);
    }

    public Hotel getHotelByEmail(String email) {
        Response response = new Response();
        return hotelRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
    }

    @Transactional
    public Hotel updateProfile(String email, UpdateHotelProfile request) {
        Hotel hotel = hotelRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Hotel not found!"));

        hotel.setHotel(request.getHotel());
        hotel.setAddress(request.getHotel());
        hotel.setCity(request.getCity());
        hotel.setPrice(request.getPrice());
        return hotelRepository.save(hotel);
    }

    public ResponseEntity<Response> roomStatus(HotelDto hotelDto){
        Response response= new Response();
        Optional<Hotel> hotelExist = hotelRepository.findByEmail(hotelDto.getEmail());
        if (!hotelExist.isPresent()){
            ErrorDetails errorDetails = new ErrorDetails(HttpStatus.NOT_FOUND,
            "Hotel not found");
            response.setError(errorDetails);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        Hotel hotel= new Hotel();
        hotel.setAvailableRooms(hotelDto.getRoomAvailable());
        response.setData(hotel.getAvailableRooms());
        return ResponseEntity.ok(response);
    }



}
