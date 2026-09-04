package com.example.personalassistant.service;

import com.example.personalassistant.entity.AdminLogin;
import com.example.personalassistant.entity.HotelLogin;
import com.example.personalassistant.entity.UserLogin;
import com.example.personalassistant.repository.AdminLoginRepository;
import com.example.personalassistant.repository.HotelLoginRepository;
import com.example.personalassistant.repository.UserLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserLoginRepository userLoginRepository;

    @Autowired
    private HotelLoginRepository hotelLoginRepository;

    @Autowired
    private AdminLoginRepository adminLoginRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. Regular User Login (Majority of queries resolve immediately on 1st query)
        UserLogin user = userLoginRepository.findByEmail(email).orElse(null);
        if (user != null) {
            return User.withUsername(user.getEmail())
                    .password(user.getPassword())
                    .roles("USER")
                    .build();
        }

        // 2. Hotel Partner Login
        HotelLogin hotel = hotelLoginRepository.findByEmail(email).orElse(null);
        if (hotel != null) {
            return User.withUsername(hotel.getEmail())
                    .password(hotel.getPassword())
                    .roles("HOTEL")
                    .build();
        }

        // 3. Admin Login
        AdminLogin admin = adminLoginRepository.findByEmail(email).orElse(null);
        if (admin != null) {
            return User.withUsername(admin.getEmail())
                    .password(admin.getPassword())
                    .roles("ADMIN")
                    .build();
        }

        throw new UsernameNotFoundException("User not found: " + email);
    }
}
