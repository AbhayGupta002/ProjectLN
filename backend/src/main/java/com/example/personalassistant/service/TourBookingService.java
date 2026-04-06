package com.example.personalassistant.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;

import com.example.personalassistant.dto.*;
import com.example.personalassistant.entity.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.personalassistant.enums.AccountEnum;
import com.example.personalassistant.enums.BookingStatus;
import com.example.personalassistant.repository.UserRepository;
import com.example.personalassistant.repository.TourBookingRepository;
import com.example.personalassistant.repository.TourPackageRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;



@Service
public class TourBookingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TourBookingRepository tourBookingRepository;

    @Autowired
    private TourPackageRepository tourPackageRepository;

    public ResponseEntity<?> bookTourByUser(TourBookingRequest request) {

        /// add 1 logic while hotel create package tour of 4 days it should be automatically triggered after 4 days tour inactive
        /// or add 1 more thing tour boarding date after tour registration will mark as active/disable/complete

        Response response = new Response();

        // 1️⃣ Validate user existence
        Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());
        if (!existingUserOpt.isPresent()) {
            ErrorDetails errorDetails = new ErrorDetails(
                    HttpStatus.BAD_REQUEST,
                    "Please register first with email: " + request.getEmail()
            );
            response.setError(errorDetails);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Optional<TourPackage> existingTour = tourPackageRepository.findById(request.getHotelId()); ///id should'be autofetch while user click on this tour
        if (existingTour.isEmpty()){ ///  add 1 more thing if hotel is active then it should be booked
            ErrorDetails errorDetails = new ErrorDetails(HttpStatus
                    .NOT_FOUND
            ,"Tour doesn't exist with this id:");
            response.setError(errorDetails);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        User hotel = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (hotel.getAccountEnum() == AccountEnum.INACTIVE ||
                hotel.getAccountEnum() == AccountEnum.SUSPENDED) {
            ErrorDetails errorDetails= new ErrorDetails(
                    HttpStatus.BAD_REQUEST,
                    "Account is suspended or Inactive"
            );
            response.setError(errorDetails);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        }

        TourPackage tourPackage = new TourPackage();
        User user = existingUserOpt.get();
        // 2️⃣ Price calculation
        double calculatedPrice = request.getTotalGuests()*tourPackage.getPrice();

        // 3️⃣ Create TourBooking entity
        TourBooking tourBooking = TourBooking.builder()
                .user(user)                               // ✅ relationship
                .hotelId(request.getHotelId())
                .name(request.getName())
                .email(request.getEmail())
                .city(request.getCity())
                .destination(request.getDestination())
                .checkInDate(request.getCheckInDate())   // ✅ fixed bug
                .totalGuests(request.getTotalGuests())
                .price(calculatedPrice) /// it should be set to the payment no modification are allowed
                .status(BookingStatus.PENDING)///⏳ pending approved while room & tour seats are available....
                .build();

        // 4️⃣ Save booking
        TourBooking savedTour = tourBookingRepository.save(tourBooking);

        // 5️⃣ Prepare response DTO
        TourBookingRequest responseDto = new TourBookingRequest();
        responseDto.setEmail(savedTour.getEmail());
        responseDto.setName(savedTour.getName());
        responseDto.setCity(savedTour.getCity());
        responseDto.setDestination(savedTour.getDestination());
        responseDto.setCheckInDate(savedTour.getCheckInDate());
        responseDto.setTotalGuests(savedTour.getTotalGuests());
        responseDto.setHotelId(savedTour.getHotelId());
        responseDto.setPrice(savedTour.getPrice());

        response.setData(responseDto);
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<Response> cancelTourBooking(Long bookingId, Long userId) {

        Response response = new Response();

        // 1️⃣ Check booking exists
        Optional<TourBooking> optionalBooking = tourBookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            ErrorDetails error = new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "Tour Booking not found with id: " + bookingId
            );
            response.setError(error);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        // 2️⃣ Check user exists
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            ErrorDetails error = new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "User not found with id: " + userId
            );
            response.setError(error);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        TourBooking booking = optionalBooking.get();
        User user = optionalUser.get();

        // 3️⃣ Check booking belongs to user
        if (!booking.getUser().getId().equals(user.getId())) {
            ErrorDetails error = new ErrorDetails(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to cancel this booking"
            );
            response.setError(error);
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        // 4️⃣ If already completed
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            ErrorDetails error = new ErrorDetails(
                    HttpStatus.BAD_REQUEST,
                    "Completed tour cannot be cancelled"
            );
            response.setError(error);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // 5️⃣ If already cancelled
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            ErrorDetails error = new ErrorDetails(
                    HttpStatus.BAD_REQUEST,
                    "Booking already cancelled"
            );
            response.setError(error);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        double refundAmount = 0.0;

        // 6️⃣ Refund Logic
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            refundAmount = booking.getPrice() * 0.70; // 70% refund
        } else if (booking.getStatus() == BookingStatus.PENDING) {
            refundAmount = booking.getPrice(); // full refund
        }

        // 7️⃣ Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());

        tourBookingRepository.save(booking);

        // 8️⃣ Success Response
        response.setData("Booking cancelled successfully. Refund Amount: ₹"
                + refundAmount + " will be credited within 3-4 working days.");
        response.setData(booking);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ✅ All pending bookings (Admin / Hotel use)
     public List<TourBooking> getPendingBookings() {
        return tourBookingRepository.findByStatus(BookingStatus.PENDING);
    }




    // ✅ Pending bookings for a hotel
    public ResponseEntity<?> getPendingBookingsByHotel(Long hotelId) {
        Response response= new Response();
        Optional<TourBooking> existingHotelId = tourBookingRepository.findById(hotelId);
        if (!existingHotelId.isPresent()){
            ErrorDetails errorDetails= new ErrorDetails(HttpStatus
                    .NOT_FOUND,
                    "Incorrect hotel id:");
            response.setError(errorDetails);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return (ResponseEntity<?>) tourBookingRepository.findByHotelIdAndStatus(hotelId, BookingStatus.PENDING);
    }



    public ResponseEntity<Response> getBookingsByUser(Long userId) {

        Response response = new Response();

        Optional<User> existingUser = userRepository.findById(userId);

        if (!existingUser.isPresent()) {

            ErrorDetails errorDetails = new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "User not found with id: " + userId
            );

            response.setError(errorDetails);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        List<TourBooking> bookings = tourBookingRepository.findByUserId(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("totalBookings", bookings.size());
        data.put("bookings", bookings);

        response.setData(data);

        return ResponseEntity.ok(response);
    }

    public TourBooking confirmBooking(Long bookingId) {
        TourBooking booking = tourBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Booking already processed");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        return tourBookingRepository.save(booking);
    }

    // ❌ Reject booking
    public TourBooking rejectBooking(Long bookingId) {
        TourBooking booking = tourBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(BookingStatus.CANCELLED);
        return tourBookingRepository.save(booking);
    }


    public ResponseEntity<Response> getAllTours() {

        Response response = new Response();

        List<TourPackage> tours = tourPackageRepository.findAll();

        if (tours.isEmpty()) {
            ErrorDetails error = new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "No tour packages found"
            );

            response.setError(error);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        response.setData(tours);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


//    public ResponseEntity<Response> cancelTourBooking(Long bookingId) {
//
//        Response response = new Response();
//
//        Optional<TourBooking> bookingOptional = tourBookingRepository.findById(bookingId);
//        if (bookingOptional.isEmpty()) {
//
//            ErrorDetails error = new ErrorDetails(
//                    HttpStatus.NOT_FOUND,
//                    "Booking not found with id: " + bookingId
//            );
//
//            response.setError(error);
//            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
//        }
//
//        TourBooking booking = bookingOptional.get();
//
//        booking.setStatus(BookingStatus.CANCELLED);
//
//        tourBookingRepository.save(booking);
//
//        response.setData("Booking cancelled successfully");
//
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
}
