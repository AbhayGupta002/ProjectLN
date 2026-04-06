package com.example.personalassistant.repository;

import com.example.personalassistant.entity.Booking;
import com.example.personalassistant.entity.Hotel;
import com.example.personalassistant.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> countByUserEmail(String email);
//    Booking findByEmail(String email);
    List<Booking> findByHotel(Hotel hotel);
    @Query(value = "SELECT * FROM bookings WHERE hotel_id = :hotelId", nativeQuery = true)
    List<Booking> findByHotelId(@Param("hotelId") Long hotelId);

    List<Booking> findByUserEmail(String email);


    boolean existsByUserAndHotel(User user, Hotel hotel);
}
