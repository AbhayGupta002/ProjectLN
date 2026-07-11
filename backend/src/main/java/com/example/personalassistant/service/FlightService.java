package com.example.personalassistant.service;

import com.example.personalassistant.dto.FlightDTO;
import com.example.personalassistant.dto.FlightBookingRequest;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.entity.Flight;
import com.example.personalassistant.entity.FlightBooking;
import com.example.personalassistant.enums.BookingStatus;
import com.example.personalassistant.enums.PaymentStatus;
import com.example.personalassistant.repository.FlightRepository;
import com.example.personalassistant.repository.FlightBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private FlightBookingRepository flightBookingRepository;

    // Add Flight
    public ResponseEntity<Response> addFlight(FlightDTO dto) {
        Response response = new Response();
        try {
            if (flightRepository.existsByFlightNumber(dto.getFlightNumber())) {
                response.setSuccess(false);
                response.setMessage("Flight number already exists.");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            Flight flight = new Flight();
            flight.setFlightNumber(dto.getFlightNumber());
            flight.setAirline(dto.getAirline());
            flight.setSource(dto.getSource());
            flight.setDestination(dto.getDestination());
            flight.setDepartureTime(dto.getDepartureTime());
            flight.setArrivalTime(dto.getArrivalTime());
            flight.setTotalSeats(dto.getTotalSeats());
            flight.setAvailableSeats(dto.getAvailableSeats());
            flight.setFare(dto.getFare());
            flight.setFlightClass(dto.getFlightClass());
            flight.setAmenities(dto.getAmenities());
            flight.setImageUrl(dto.getImageUrl());
            flight.setStatus(true);

            Flight saved = flightRepository.save(flight);
            response.setSuccess(true);
            response.setMessage("Flight added successfully.");
            response.setData(saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Get All Flights
    public ResponseEntity<Response> getAllFlights() {
        Response response = new Response();
        try {
            List<Flight> flights = flightRepository.findByStatusTrue();
            response.setSuccess(true);
            response.setData(flights);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Search Flights
    public ResponseEntity<Response> searchFlights(String source, String destination) {
        Response response = new Response();
        try {
            List<Flight> flights = flightRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(source, destination);
            response.setSuccess(true);
            response.setData(flights);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Book Flight
    @Transactional
    public ResponseEntity<Response> bookFlight(FlightBookingRequest request) {
        Response response = new Response();
        try {
            Flight flight = flightRepository.findById(request.getFlightId()).orElse(null);
            if (flight == null || !flight.getStatus()) {
                response.setSuccess(false);
                response.setMessage("Flight not found or inactive.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (flight.getAvailableSeats() < request.getNumberOfSeats()) {
                response.setSuccess(false);
                response.setMessage("Insufficient available seats.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Decrement seats
            flight.setAvailableSeats(flight.getAvailableSeats() - request.getNumberOfSeats());
            flightRepository.save(flight);

            // Create Booking
            FlightBooking booking = new FlightBooking();
            booking.setUserId(request.getUserId());
            booking.setFlightId(request.getFlightId());
            booking.setPassengerName(request.getPassengerName());
            booking.setPassengerAge(request.getPassengerAge());
            booking.setPassengerGender(request.getPassengerGender());
            booking.setNumberOfSeats(request.getNumberOfSeats());
            booking.setJourneyDate(request.getJourneyDate());
            booking.setTotalFare(flight.getFare() * request.getNumberOfSeats());
            booking.setBookingStatus(BookingStatus.CONFIRMED); // For demo/instant confirmation
            booking.setPaymentStatus(PaymentStatus.SUCCESS);
            booking.setBookingDate(LocalDateTime.now());

            FlightBooking savedBooking = flightBookingRepository.save(booking);
            response.setSuccess(true);
            response.setMessage("Flight booked successfully.");
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
            List<FlightBooking> bookings = flightBookingRepository.findByUserId(userId);
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
    public ResponseEntity<Response> cancelFlightBooking(Long bookingId) {
        Response response = new Response();
        try {
            FlightBooking booking = flightBookingRepository.findById(bookingId).orElse(null);
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
            Flight flight = flightRepository.findById(booking.getFlightId()).orElse(null);
            if (flight != null) {
                flight.setAvailableSeats(flight.getAvailableSeats() + booking.getNumberOfSeats());
                flightRepository.save(flight);
            }

            booking.setBookingStatus(BookingStatus.CANCELLED);
            flightBookingRepository.save(booking);

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
