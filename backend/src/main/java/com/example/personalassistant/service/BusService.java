package com.example.personalassistant.service;

import com.example.personalassistant.dto.BusDTO;
import com.example.personalassistant.dto.BusBookingRequest;
import com.example.personalassistant.dto.ErrorDetails;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.entity.Bus;
import com.example.personalassistant.entity.BusBooking;
import com.example.personalassistant.enums.BookingStatus;
import com.example.personalassistant.enums.PaymentStatus;
import com.example.personalassistant.repository.BusRepository;
import com.example.personalassistant.repository.BusBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BusService {

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private BusBookingRepository busBookingRepository;

    // Add Bus
    public ResponseEntity<Response> addBus(BusDTO dto) {

        Response response = new Response();

        try {

            // Duplicate Bus Number
            if (busRepository.existsByBusNumber(dto.getBusNumber())) {

                ErrorDetails error = new ErrorDetails(
                        HttpStatus.CONFLICT,
                        "Bus number already exists."
                );

                response.setError(error);

                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // Duplicate Bus Name
            if (busRepository.existsByBusName(dto.getBusName())) {

                ErrorDetails error = new ErrorDetails(
                        HttpStatus.CONFLICT,
                        "Bus name already exists."
                );

                response.setError(error);

                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // Source & Destination
            if (dto.getSource().equalsIgnoreCase(dto.getDestination())) {

                ErrorDetails error = new ErrorDetails(
                        HttpStatus.BAD_REQUEST,
                        "Source and Destination cannot be same."
                );

                response.setError(error);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Seat Validation
            if (dto.getAvailableSeats() > dto.getTotalSeats()) {

                ErrorDetails error = new ErrorDetails(
                        HttpStatus.BAD_REQUEST,
                        "Available seats cannot be greater than total seats."
                );

                response.setError(error);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Time Validation
            if (!dto.getArrivalTime().isAfter(dto.getDepartureTime())) {

                ErrorDetails error = new ErrorDetails(
                        HttpStatus.BAD_REQUEST,
                        "Arrival time must be after departure time."
                );

                response.setError(error);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Bus bus = new Bus();

            bus.setBusName(dto.getBusName());
            bus.setBusNumber(dto.getBusNumber());
            bus.setOperatorName(dto.getOperatorName());
            bus.setSource(dto.getSource());
            bus.setDestination(dto.getDestination());
            bus.setDepartureTime(dto.getDepartureTime());
            bus.setArrivalTime(dto.getArrivalTime());
            bus.setTotalSeats(dto.getTotalSeats());
            bus.setAvailableSeats(dto.getAvailableSeats());
            bus.setFare(dto.getFare());
            bus.setBusType(dto.getBusType());
            bus.setAmenities(dto.getAmenities());
            bus.setImageUrl(dto.getImageUrl());
            bus.setStatus(true);

            Bus savedBus = busRepository.save(bus);

            response.setMessage("Bus added successfully.");
            response.setData(savedBus);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {

            ErrorDetails error = new ErrorDetails(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );

            response.setError(error);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    // Get All Buses
    public ResponseEntity<Response> getAllBuses() {

        Response response = new Response();

        try {

            List<Bus> buses = busRepository.findByStatusTrue();

            response.setData("Bus list fetched successfully.");
            response.setData(buses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            ErrorDetails error = new ErrorDetails(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );

            response.setError(error);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    public ResponseEntity<Response> getBusById(Long id) {

        Response response = new Response();

        try {

            Bus bus = busRepository.findById(id).orElse(null);

            if (bus == null) {

                ErrorDetails error = new ErrorDetails(
                        HttpStatus.NOT_FOUND,
                        "Bus not found."
                );

                response.setError(error);

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(response);
            }

            response.setData("Bus fetched successfully.");
            response.setData(bus);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            ErrorDetails error = new ErrorDetails(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );

            response.setError(error);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }


    public ResponseEntity<Response> deleteBus(Long id) {

        Response response = new Response();

        try {

            Bus bus = busRepository.findById(id).orElse(null);

            if (bus == null) {

                ErrorDetails error = new ErrorDetails(
                        HttpStatus.NOT_FOUND,
                        "Bus not found."
                );

                response.setError(error);

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(response);
            }

            busRepository.delete(bus);

            response.setData("Bus deleted successfully.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            ErrorDetails error = new ErrorDetails(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );

            response.setError(error);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    public ResponseEntity<Response> searchBus(String source,
                                              String destination,
                                              String date,
                                              String query) {

        Response response = new Response();

        try {
            List<Bus> buses;
            String src = (source != null) ? source.trim() : "";
            String dst = (destination != null) ? destination.trim() : "";
            String q = (query != null) ? query.trim() : "";

            if (!src.isEmpty() && !dst.isEmpty()) {
                buses = busRepository.findBySourceContainingIgnoreCaseAndDestinationContainingIgnoreCase(src, dst);
                if (buses.isEmpty()) {
                    buses = busRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(src, dst);
                }
            } else if (!q.isEmpty()) {
                buses = busRepository.findBySourceContainingIgnoreCaseOrDestinationContainingIgnoreCaseOrBusNameContainingIgnoreCaseOrOperatorNameContainingIgnoreCaseOrBusNumberContainingIgnoreCase(q, q, q, q, q);
            } else if (!src.isEmpty()) {
                buses = busRepository.findBySourceContainingIgnoreCaseOrDestinationContainingIgnoreCaseOrBusNameContainingIgnoreCaseOrOperatorNameContainingIgnoreCaseOrBusNumberContainingIgnoreCase(src, src, src, src, src);
            } else if (!dst.isEmpty()) {
                buses = busRepository.findBySourceContainingIgnoreCaseOrDestinationContainingIgnoreCaseOrBusNameContainingIgnoreCaseOrOperatorNameContainingIgnoreCaseOrBusNumberContainingIgnoreCase(dst, dst, dst, dst, dst);
            } else {
                buses = busRepository.findByStatusTrue();
            }

            response.setMessage(buses.size() + " buses found.");
            response.setData(buses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            ErrorDetails error = new ErrorDetails(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );

            response.setError(error);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }

    }

    public ResponseEntity<Response> searchBus(String source, String destination) {
        return searchBus(source, destination, null, null);
    }

    public ResponseEntity<Response> searchByName(String busName) {

        Response response = new Response();

        try {

            List<Bus> buses = busRepository
                    .findByBusNameContainingIgnoreCase(busName);

            response.setData("Bus list fetched successfully.");
            response.setData(buses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            ErrorDetails error = new ErrorDetails(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );

            response.setError(error);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }

    }


    public ResponseEntity<Response> searchByOperator(String operatorName) {

        Response response = new Response();

        try {

            List<Bus> buses = busRepository
                    .findByOperatorNameContainingIgnoreCase(operatorName);

            response.setData("Bus list fetched successfully.");
            response.setData(buses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            ErrorDetails error = new ErrorDetails(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );

            response.setError(error);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }

    }

    public ResponseEntity<Response> searchByBusType(String busType) {

        Response response = new Response();

        try {

            List<Bus> buses = busRepository
                    .findByBusTypeIgnoreCase(busType);

            response.setData("Bus list fetched successfully.");
            response.setData(buses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            ErrorDetails error = new ErrorDetails(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );

            response.setError(error);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }

    }

    public ResponseEntity<Response> updateBus(Long id, BusDTO dto) {

        Response response = new Response();

        try {

            Bus bus = busRepository.findById(id).orElse(null);

            if (bus == null) {

                ErrorDetails error = new ErrorDetails(
                        HttpStatus.NOT_FOUND,
                        "Bus not found."
                );

                response.setError(error);

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Check Duplicate Bus Number
            if (!bus.getBusNumber().equalsIgnoreCase(dto.getBusNumber())
                    && busRepository.existsByBusNumber(dto.getBusNumber())) {

                ErrorDetails error = new ErrorDetails(
                        HttpStatus.CONFLICT,
                        "Bus number already exists."
                );

                response.setError(error);

                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // Check Duplicate Bus Name
            if (!bus.getBusName().equalsIgnoreCase(dto.getBusName())
                    && busRepository.existsByBusName(dto.getBusName())) {

                ErrorDetails error = new ErrorDetails(
                        HttpStatus.CONFLICT,
                        "Bus name already exists."
                );

                response.setError(error);

                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // Source & Destination Validation
            if (dto.getSource().equalsIgnoreCase(dto.getDestination())) {

                ErrorDetails error = new ErrorDetails(
                        HttpStatus.BAD_REQUEST,
                        "Source and destination cannot be same."
                );

                response.setError(error);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Seat Validation
            if (dto.getAvailableSeats() > dto.getTotalSeats()) {

                ErrorDetails error = new ErrorDetails(
                        HttpStatus.BAD_REQUEST,
                        "Available seats cannot be greater than total seats."
                );

                response.setError(error);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Time Validation
            if (!dto.getArrivalTime().isAfter(dto.getDepartureTime())) {

                ErrorDetails error = new ErrorDetails(
                        HttpStatus.BAD_REQUEST,
                        "Arrival time must be after departure time."
                );

                response.setError(error);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Update Fields
            bus.setBusName(dto.getBusName());
            bus.setBusNumber(dto.getBusNumber());
            bus.setOperatorName(dto.getOperatorName());
            bus.setSource(dto.getSource());
            bus.setDestination(dto.getDestination());
            bus.setDepartureTime(dto.getDepartureTime());
            bus.setArrivalTime(dto.getArrivalTime());
            bus.setTotalSeats(dto.getTotalSeats());
            bus.setAvailableSeats(dto.getAvailableSeats());
            bus.setFare(dto.getFare());
            bus.setBusType(dto.getBusType());
            bus.setAmenities(dto.getAmenities());
            bus.setImageUrl(dto.getImageUrl());
            bus.setStatus(dto.getStatus());

            Bus updatedBus = busRepository.save(bus);

            response.setMessage("Bus updated successfully.");
            response.setData(updatedBus);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            ErrorDetails error = new ErrorDetails(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );

            response.setError(error);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    // Book Bus
    @Transactional
    public ResponseEntity<Response> bookBus(BusBookingRequest request) {
        Response response = new Response();
        try {
            Bus bus = busRepository.findById(request.getBusId()).orElse(null);
            if (bus == null || !bus.getStatus()) {
                response.setSuccess(false);
                response.setMessage("Bus not found or inactive.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (bus.getAvailableSeats() < request.getNumberOfSeats()) {
                response.setSuccess(false);
                response.setMessage("Insufficient available seats.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Decrement seats
            bus.setAvailableSeats(bus.getAvailableSeats() - request.getNumberOfSeats());
            busRepository.save(bus);

            // Create Booking
            BusBooking booking = new BusBooking();
            booking.setUserId(request.getUserId());
            booking.setBusId(request.getBusId());
            booking.setPassengerName(request.getPassengerName());
            booking.setPassengerAge(request.getPassengerAge());
            booking.setPassengerGender(request.getPassengerGender());
            booking.setNumberOfSeats(request.getNumberOfSeats());
            booking.setJourneyDate(request.getJourneyDate());
            booking.setTotalFare(bus.getFare() * request.getNumberOfSeats());
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            booking.setPaymentStatus(PaymentStatus.SUCCESS);
            booking.setBookingDate(LocalDateTime.now());

            BusBooking savedBooking = busBookingRepository.save(booking);
            response.setSuccess(true);
            response.setMessage("Bus booked successfully.");
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
            List<BusBooking> bookings = busBookingRepository.findByUserId(userId);
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
    public ResponseEntity<Response> cancelBusBooking(Long bookingId) {
        Response response = new Response();
        try {
            BusBooking booking = busBookingRepository.findById(bookingId).orElse(null);
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
            Bus bus = busRepository.findById(booking.getBusId()).orElse(null);
            if (bus != null) {
                bus.setAvailableSeats(bus.getAvailableSeats() + booking.getNumberOfSeats());
                busRepository.save(bus);
            }

            booking.setBookingStatus(BookingStatus.CANCELLED);
            busBookingRepository.save(booking);

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
