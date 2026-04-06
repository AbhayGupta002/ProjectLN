package com.example.personalassistant.repository;

import java.util.Optional;
import com.example.personalassistant.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AdminRepository extends JpaRepository <Admin,String>{
    Optional<Admin> findByEmail(String email);
}
