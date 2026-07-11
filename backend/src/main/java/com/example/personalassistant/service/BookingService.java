package com.example.personalassistant.service;

import com.example.personalassistant.dto.ErrorDetails;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.entity.Booking;
import com.example.personalassistant.entity.Hotel;
import com.example.personalassistant.entity.User;
import com.example.personalassistant.enums.BookingStatus;
import com.example.personalassistant.repository.UserRepository;
import com.example.personalassistant.repository.HotelRepository;
import com.example.personalassistant.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public ResponseEntity<Response> bookHotel(String email, Long hotelId) {

        Response response = new Response();

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            response.setError(new ErrorDetails(
                    HttpStatus.UNAUTHORIZED,
                    "User not logged in or invalid token"
            ));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
        if (hotel == null) {
            response.setError(new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "Hotel not found"
            ));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        boolean alreadyBooked = bookingRepository.existsByUserAndHotel(user, hotel);

        if (alreadyBooked) {
            response.setError(new ErrorDetails(
                    HttpStatus.CONFLICT,
                    "You have already booked this hotel"
            ));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        try {

            Booking booking = new Booking();
            booking.setUser(user);
            booking.setHotel(hotel);

            LocalDateTime checkIn = LocalDateTime.now();
            LocalDateTime checkOut = checkIn.plusDays(1); // example: 1 night stay

            booking.setCheckIn(checkIn);
            booking.setCheckOut(checkOut);

            booking.setRoomsNumber(1); // default room count

            // Calculate amount
            double price = Double.parseDouble(hotel.getPrice()); // assuming price is string
            booking.setAmount(price);

            // Optional fields
            booking.setPaymentStatus("PENDING");
            booking.setBookingStatus(BookingStatus.CONFIRMED);

            // 5️⃣ Save booking
            bookingRepository.save(booking);

            // 6️⃣ Prepare response
            Map<String, Object> data = new HashMap<>();
            data.put("message", "Hotel booked successfully");
            data.put("user", user.getEmail());
            data.put("hotel", hotel.getHotel());
            data.put("checkIn", checkIn);
            data.put("checkOut", checkOut);
            data.put("amount", price);

            response.setData(data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.setError(new ErrorDetails(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error booking hotel: " + e.getMessage()
            ));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }//    public int countUserBookings(String email) {
//        return bookingRepository.countByUserEmail(email);
//    }

    public ResponseEntity<Response> getAllBookingsByHotel(String email) {

        Response response = new Response();

        Hotel hotel = hotelRepository.findByEmail(email).orElse(null);

        if (hotel == null) {
            response.setError(new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "Hotel not found"
            ));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        List<Booking> bookings =
                bookingRepository.findByHotelId(hotel.getId());

        System.out.println("Hotel ID: " + hotel.getId());
        System.out.println("Bookings found: " + bookings.size());

        Map<String, Object> data = new HashMap<>();
        data.put("hotelName", hotel.getHotel());
        data.put("hotelId", hotel.getId());
        data.put("totalBookings", bookings.size());
        data.put("bookings", bookings);

        response.setData(data);

        return ResponseEntity.ok(response);
    }
    public ResponseEntity<Response> getUserBookings(String email) {
        Response response = new Response();

        List<Booking> bookings = bookingRepository.findByUserEmail(email);

        if (bookings.isEmpty()) {

            ErrorDetails error = new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "No bookings found for email: " + email
            );

            response.setError(error);

            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        response.setData(bookings);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



}

