package com.example.personalassistant.service;

import com.example.personalassistant.entity.Cab;
import com.example.personalassistant.entity.CabBooking;
import com.example.personalassistant.entity.User;
import com.example.personalassistant.dto.CabBookingRequest;
import com.example.personalassistant.enums.BookingStatus;
import com.example.personalassistant.enums.PaymentStatus;
import com.example.personalassistant.repository.CabRepository;
import com.example.personalassistant.repository.CabBookingRepository;
import com.example.personalassistant.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CabService {

    @Autowired
    private CabRepository cabRepository;

    @Autowired
    private CabBookingRepository cabBookingRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Cab> getCabsByCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            return cabRepository.findByAvailableTrue();
        }
        return cabRepository.findByCityIgnoreCaseAndAvailableTrue(city.trim());
    }

    public List<Cab> getAllAvailableCabs() {
        return cabRepository.findByAvailableTrue();
    }

    public Optional<Cab> getCabById(Long id) {
        return cabRepository.findById(id);
    }

    @Transactional
    public CabBooking bookCab(CabBookingRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        Cab cab = cabRepository.findById(request.getCabId())
                .orElseThrow(() -> new RuntimeException("Cab not found with id: " + request.getCabId()));

        if (!cab.getAvailable()) {
            throw new RuntimeException("Cab is currently unavailable");
        }

        double distance = (request.getEstimatedDistanceKm() != null && request.getEstimatedDistanceKm() > 0)
                ? request.getEstimatedDistanceKm() : 15.0;
        double fare = cab.getBaseFare() + (distance * cab.getPerKmRate());

        CabBooking booking = new CabBooking();
        booking.setUserId(user.getId());
        booking.setCabId(cab.getId());
        booking.setCustomerName(request.getCustomerName() != null ? request.getCustomerName() : user.getName());
        booking.setCustomerPhone(request.getCustomerPhone() != null ? request.getCustomerPhone() : user.getMobile());
        booking.setPickupLocation(request.getPickupLocation());
        booking.setDropLocation(request.getDropLocation());
        booking.setBookingDate(LocalDateTime.now());
        booking.setPickupTime(request.getPickupTime() != null ? request.getPickupTime() : LocalDateTime.now().plusHours(1));
        booking.setEstimatedFare(fare);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);

        cab.setAvailable(false); // Booked
        cabRepository.save(cab);

        return cabBookingRepository.save(booking);
    }

    @Transactional
    public CabBooking cancelCabBooking(Long bookingId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        CabBooking booking = cabBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        if (!booking.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: You can only cancel your own bookings");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);

        cabRepository.findById(booking.getCabId()).ifPresent(cab -> {
            cab.setAvailable(true);
            cabRepository.save(cab);
        });

        return cabBookingRepository.save(booking);
    }

    public List<CabBooking> getUserCabBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        return cabBookingRepository.findByUserId(user.getId());
    }
}
