package com.example.personalassistant.repository;

import com.example.personalassistant.entity.TourBooking;
import com.example.personalassistant.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TourBookingRepository extends JpaRepository<TourBooking, Long> {
    List<TourBooking> findByUserId(Long userId);

    List<TourBooking> findByStatus(BookingStatus status);

    // 🔹 Fetch pending bookings for a specific hotel
    List<TourBooking> findByHotelIdAndStatus(Long hotelId, BookingStatus status);

    @Query(
            value = "SELECT h FROM Hotel h WHERE LOWER(h.location) = LOWER(:location)"
            ,nativeQuery = true
    )
    List<TourBooking> findByLocationIgnoreCase(String location);

    List<TourBooking> findByEmail(String email);


}
