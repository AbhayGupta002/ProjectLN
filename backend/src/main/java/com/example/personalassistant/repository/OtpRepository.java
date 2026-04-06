package com.example.personalassistant.repository;

import java.util.Optional;
import com.example.personalassistant.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OtpRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByEmail(String email);
}