package com.example.personalassistant.repository;

import java.util.Optional;
import com.example.personalassistant.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface OtpRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByEmail(String email);
    Optional<OtpVerification> findTopByEmailOrderByIdDesc(String email);

    @Modifying
    @Transactional
    void deleteByEmail(String email);
}