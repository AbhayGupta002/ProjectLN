package com.example.personalassistant.repository;

import com.example.personalassistant.entity.AdminLogin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminLoginRepository extends JpaRepository<AdminLogin, String> {
    Optional<AdminLogin> findByEmail(String email);
}
