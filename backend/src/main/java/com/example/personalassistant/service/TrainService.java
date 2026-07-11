package com.example.personalassistant.service;

import com.example.personalassistant.dto.TrainDTO;
import com.example.personalassistant.dto.TrainBookingRequest;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.entity.Train;
import com.example.personalassistant.entity.TrainBooking;
import com.example.personalassistant.enums.BookingStatus;
import com.example.personalassistant.enums.PaymentStatus;
import com.example.personalassistant.repository.TrainRepository;
import com.example.personalassistant.repository.TrainBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private TrainBookingRepository trainBookingRepository;

    // Add Train
    public ResponseEntity<Response> addTrain(TrainDTO dto) {
        Response response = new Response();
        try {
            if (trainRepository.existsByTrainNumber(dto.getTrainNumber())) {
                response.setSuccess(false);
                response.setMessage("Train number already exists.");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            Train train = new Train();
            train.setTrainNumber(dto.getTrainNumber());
            train.setTrainName(dto.getTrainName());
            train.setSource(dto.getSource());
            train.setDestination(dto.getDestination());
            train.setDepartureTime(dto.getDepartureTime());
            train.setArrivalTime(dto.getArrivalTime());
            train.setTotalSeats(dto.getTotalSeats());
            train.setAvailableSeats(dto.getAvailableSeats());
            train.setFare(dto.getFare());
            train.setTrainClass(dto.getTrainClass());
            train.setAmenities(dto.getAmenities());
            train.setImageUrl(dto.getImageUrl());
            train.setStatus(true);

            Train saved = trainRepository.save(train);
            response.setSuccess(true);
            response.setMessage("Train added successfully.");
            response.setData(saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Get All Trains
    public ResponseEntity<Response> getAllTrains() {
        Response response = new Response();
        try {
            List<Train> trains = trainRepository.findByStatusTrue();
            response.setSuccess(true);
            response.setData(trains);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Search Trains
    public ResponseEntity<Response> searchTrains(String source, String destination) {
        Response response = new Response();
        try {
            List<Train> trains = trainRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(source, destination);
            response.setSuccess(true);
            response.setData(trains);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Book Train
    @Transactional
    public ResponseEntity<Response> bookTrain(TrainBookingRequest request) {
        Response response = new Response();
        try {
            Train train = trainRepository.findById(request.getTrainId()).orElse(null);
            if (train == null || !train.getStatus()) {
                response.setSuccess(false);
                response.setMessage("Train not found or inactive.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (train.getAvailableSeats() < request.getNumberOfSeats()) {
                response.setSuccess(false);
                response.setMessage("Insufficient available seats.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Decrement seats
            train.setAvailableSeats(train.getAvailableSeats() - request.getNumberOfSeats());
            trainRepository.save(train);

            // Create Booking
            TrainBooking booking = new TrainBooking();
            booking.setUserId(request.getUserId());
            booking.setTrainId(request.getTrainId());
            booking.setPassengerName(request.getPassengerName());
            booking.setPassengerAge(request.getPassengerAge());
            booking.setPassengerGender(request.getPassengerGender());
            booking.setNumberOfSeats(request.getNumberOfSeats());
            booking.setJourneyDate(request.getJourneyDate());
            booking.setTotalFare(train.getFare() * request.getNumberOfSeats());
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            booking.setPaymentStatus(PaymentStatus.SUCCESS);
            booking.setBookingDate(LocalDateTime.now());

            TrainBooking savedBooking = trainBookingRepository.save(booking);
            response.setSuccess(true);
            response.setMessage("Train booked successfully.");
            response.setData(savedBooking);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Get User Bookings
    public ResponseEntity<Response> getUserBookings(Long userId) {
        Response response = new Response();
        try {
            List<TrainBooking> bookings = trainBookingRepository.findByUserId(userId);
            response.setSuccess(true);
            response.setData(bookings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Cancel Booking
    @Transactional
    public ResponseEntity<Response> cancelTrainBooking(Long bookingId) {
        Response response = new Response();
        try {
            TrainBooking booking = trainBookingRepository.findById(bookingId).orElse(null);
            if (booking == null) {
                response.setSuccess(false);
                response.setMessage("Booking not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
                response.setSuccess(false);
                response.setMessage("Booking is already cancelled.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Restore seats
            Train train = trainRepository.findById(booking.getTrainId()).orElse(null);
            if (train != null) {
                train.setAvailableSeats(train.getAvailableSeats() + booking.getNumberOfSeats());
                trainRepository.save(train);
            }

            booking.setBookingStatus(BookingStatus.CANCELLED);
            trainBookingRepository.save(booking);

            response.setSuccess(true);
            response.setMessage("Booking cancelled successfully.");
            response.setData(booking);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
