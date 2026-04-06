package com.example.personalassistant.repository;

import java.util.Optional;
import com.example.personalassistant.entity.HotelLogin;
import org.springframework.data.jpa.repository.JpaRepository;


public interface HotelLoginRepository extends JpaRepository<HotelLogin, String> {
    boolean existsByEmail(String email);
    Optional<HotelLogin> findByEmail(String email);
}
