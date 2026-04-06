package com.example.personalassistant.repository;

import java.util.Optional;
import com.example.personalassistant.entity.UserLogin;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserLoginRepository extends JpaRepository<UserLogin, String> {
    boolean existsByEmail(String email);
    Optional<UserLogin> findByEmail(String email);

}
