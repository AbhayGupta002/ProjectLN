package com.example.personalassistant.service;

import com.example.personalassistant.dto.ErrorDetails;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.entity.Hotel;
import com.example.personalassistant.entity.User;
import com.example.personalassistant.enums.AccountEnum;
import com.example.personalassistant.repository.HotelRepository;
import com.example.personalassistant.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminDashboardService {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    // -------- SUSPEND HOTEL --------
    @Transactional
    public ResponseEntity<Response> suspendHotelAccount(Long hotelId) {
        Response response = new Response();

        Optional<Hotel> hotelOpt = hotelRepository.findById(hotelId);

        if (hotelOpt.isEmpty()) {
            response.setError(new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "Hotel not found with id: " + hotelId
            ));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Hotel hotel = hotelOpt.get();

        if (hotel.getAccountEnum() == AccountEnum.SUSPENDED) {
            response.setData("Hotel already suspended");
            return ResponseEntity.ok(response);
        }

        hotel.setAccountEnum(AccountEnum.SUSPENDED);
        hotelRepository.save(hotel);

        response.setData("Hotel suspended successfully");
        return ResponseEntity.ok(response);
    }

    // -------- SUSPEND USER --------
    @Transactional
    public ResponseEntity<Response> suspendUserAccount(Long id) {
        Response response = new Response();

        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            response.setError(new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "User not found with id: " + id
            ));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        User user = userOpt.get();

        if (user.getAccountEnum() == AccountEnum.SUSPENDED) {
            response.setData("User already suspended");
            return ResponseEntity.ok(response);
        }

        user.setAccountEnum(AccountEnum.SUSPENDED);
        userRepository.save(user);

        response.setData("User suspended successfully");
        return ResponseEntity.ok(response);
    }

    // -------- GET ALL HOTELS --------
    public ResponseEntity<Response> getAllHotels() {
        Response response = new Response();

        List<Hotel> hotels = hotelRepository.findAll();

        if (hotels.isEmpty()) {
            response.setError(new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "No hotels found"
            ));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.setData(hotels);
        return ResponseEntity.ok(response);
    }

    // -------- ACTIVE HOTELS --------
    public ResponseEntity<Response> getActiveHotels() {
        Response response = new Response();

        List<Hotel> hotels = hotelRepository.findByAccountEnum(AccountEnum.ACTIVE);

        if (hotels.isEmpty()) {
            response.setError(new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "No active hotels found"
            ));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.setData(hotels);
        return ResponseEntity.ok(response);
    }

    // -------- INACTIVE HOTELS --------
    public ResponseEntity<Response> getInactiveHotels() {
        Response response = new Response();

        List<Hotel> hotels = hotelRepository.findByAccountEnum(AccountEnum.INACTIVE);

        if (hotels.isEmpty()) {
            response.setError(new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "No inactive hotels found"
            ));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.setData(hotels);
        return ResponseEntity.ok(response);
    }

    // -------- ACTIVE USERS --------
    public ResponseEntity<Response> getActiveUsers() {
        Response response = new Response();

        List<User> users = userRepository.findByAccountEnum(AccountEnum.ACTIVE);

        if (users.isEmpty()) {
            response.setError(new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "No active users found"
            ));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.setData(users);
        return ResponseEntity.ok(response);
    }

    // -------- GET HOTEL BY ID --------
    public ResponseEntity<Response> getHotelById(Long id) {
        Response response = new Response();

        Optional<Hotel> hotelOpt = hotelRepository.findById(id);

        if (hotelOpt.isEmpty()) {
            response.setError(new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "Hotel not found with id: " + id
            ));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.setData(hotelOpt.get());
        return ResponseEntity.ok(response);
    }
}

//package com.example.personalassistant.service;
//
//import com.example.personalassistant.dto.AdminDto;
//import com.example.personalassistant.dto.ErrorDetails;
//import com.example.personalassistant.dto.Response;
//import com.example.personalassistant.entity.AdminLogin;
//import com.example.personalassistant.entity.Hotel;
//import com.example.personalassistant.entity.User;
//import com.example.personalassistant.enums.AccountEnum;
//import com.example.personalassistant.repository.*;
//import com.example.personalassistant.security.JwtUtil;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class AdminDashboardService {
//
//    @Autowired
//    private AdminLoginRepository adminLoginRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Autowired
//    private HotelRepository hotelRepository;
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    @Autowired
//    private AdminRepository adminRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Transactional
//    public ResponseEntity<Response> suspendHotelAccount(Long hotelId) {
//        Response response = new Response();
//        Optional<Hotel> hotelOpt = hotelRepository.findById(hotelId);
//        if (hotelOpt.isEmpty()) {
//            ErrorDetails errorDetails= new ErrorDetails(HttpStatus.NOT_FOUND,
//            "Id not found with this id"+hotelId);
//            response.setError(errorDetails);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//
//        Hotel hotel = hotelOpt.get();
//        if (hotel.getAccountEnum() == AccountEnum.SUSPENDED) {
//            response.setData("Hotel already suspended");
//            return ResponseEntity.ok(response);
//        }
//
//        hotel.setAccountEnum(AccountEnum.SUSPENDED);
//        hotelRepository.save(hotel);
//
//        response.setData("Hotel suspended successfully");
//        return ResponseEntity.ok(response);
//    }
//
//    @Transactional
//    public ResponseEntity<Response> suspendUserAccount(Long id) {
//        Response response = new Response();
//        try {
//
//            int updatedRows = userRepository.suspendUser(id);
//
//            if (updatedRows == 0) {
//                response.setError(
//                        new ErrorDetails(
//                                HttpStatus.NOT_FOUND,
//                                "User not found with id: " + id
//                        )
//                );
//
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//            }
//
//            response.setData("User suspended successfully");
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//
//            response.setError(
//                    new ErrorDetails(
//                            HttpStatus.INTERNAL_SERVER_ERROR,
//                            "Error suspending user: " + e.getMessage()
//                    )
//            );
//
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }
//        public ResponseEntity<Response> getAllHotels(){
//        Response response = new Response();
//        Optional<AdminLogin> login = adminLoginRepository.findByEmail(email);
//        if(!login.isPresent()){
//            ErrorDetails errorDetails = new ErrorDetails(HttpStatus
//                    .NOT_FOUND,
//                    "Incorrect email id"+email
//                    );
//            response.setError(errorDetails);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//            List<Hotel> hotels = hotelRepository.findAll();
//            response.setData(hotels);
//            return ResponseEntity.ok(response);
//    }
//
//    public ResponseEntity<Response> getActiveHotels() {
//        Response response = new Response();
//        List<Hotel> hotels =
//                hotelRepository.findByAccountEnum(AccountEnum.ACTIVE);
//        if (hotels.isEmpty()) {
//            ErrorDetails errorDetails = new ErrorDetails(HttpStatus
//                    .NOT_FOUND
//                    ,"No active Users found");
//            response.setError(errorDetails);
//            return ResponseEntity.ok(response);
//        }
//        response.setData(hotels);
//        return ResponseEntity.ok(response);
//    }
//
//
//    public ResponseEntity<Response> getActiveUsers() {
//        Response response = new Response();
//        List<User> user =
//        userRepository.findByAccountEnum(AccountEnum.ACTIVE);
//        if (user.isEmpty()) {
//            ErrorDetails errorDetails = new ErrorDetails(HttpStatus
//                    .NOT_FOUND
//                    ,"No active Users found");
//            response.setError(errorDetails);
//            return ResponseEntity.ok(response);
//        }
//        response.setData(user);
//        return ResponseEntity.ok(response);
//    }
//
//    public ResponseEntity<Response> getInactiveHotels() {
//        AdminDto adminDto = new AdminDto();
//        Response response = new Response();
//        Optional<AdminLogin> checkAdmin =
//                adminLoginRepository.findByEmail(adminDto.getEmail());
//        if (checkAdmin.isEmpty()) {
//            ErrorDetails errorDetails = new ErrorDetails(
//                    HttpStatus.NOT_FOUND,
//                    "Admin not found"
//            );
//            response.setError(errorDetails);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//
//// Get admin from DB
//        AdminLogin adminLogin = checkAdmin.get();
//
//// Verify password (IMPORTANT if using BCrypt)
//        if (!passwordEncoder.matches(adminDto.getPassword(), adminLogin.getPassword())) {
//            ErrorDetails errorDetails = new ErrorDetails(
//                    HttpStatus.BAD_REQUEST,
//                    "Incorrect password"
//            );
//            response.setError(errorDetails);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        }
//
//// Fetch inactive hotels
//        List<Hotel> hotels =
//                hotelRepository.findByAccountEnum(AccountEnum.INACTIVE);
//        if (hotels.isEmpty()) {
//            ErrorDetails errorDetails = new ErrorDetails(
//                    HttpStatus.NOT_FOUND,
//                    "No inactive hotels found"
//            );
//            response.setError(errorDetails);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//        response.setData(hotels);
//        return ResponseEntity.ok(response);
//    }
//
//
//
//    public ResponseEntity<Response> getHotelById(Long id,String name){
//        Response response = new Response();
//        Optional<Hotel> optionalHotel = hotelRepository.findById(id);
//        if (!optionalHotel.isPresent()){
//            ErrorDetails errorDetails = new ErrorDetails(HttpStatus
//                    .NOT_FOUND
//            ,"hotel does not exist with this id:"+id);
//            response.setError(errorDetails);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//
//        AdminDto adminDto = new AdminDto();
//        optionalHotel = hotelRepository.findById(adminDto.getId());
//        response.setData(optionalHotel);
//        return ResponseEntity.ok(response);
//    }
//
//
////    public ResponseEntity<Response> searchHotelsByName(AdminDto hotel) {
////        Response response = new Response();
////        List<Hotel> hotels =
////                hotelRepository.findByNameStartingWithIgnoreCase(hotel.getName());
////        if (hotels.isEmpty()) {
////            response.setError(new ErrorDetails(HttpStatus
////                    .NOT_FOUND
////            ,"Hotel not found"));
////            return ResponseEntity.ok(response);
////        }
////        AdminDto adminDto = new AdminDto();
////        Hotel hotelData = new Hotel();
////        hotelData.setHotel(adminDto.getHotel());
////        hotelData.setEmail(adminDto.getEmail());
////        hotelData.setId(adminDto.getId());
////
////        response.setData(hotels);
////        return ResponseEntity.ok(response);
////    }
//
//}
