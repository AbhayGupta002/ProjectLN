package com.example.personalassistant.service;

import com.example.personalassistant.entity.HotelLogin;
import com.example.personalassistant.entity.UserLogin;
import com.example.personalassistant.repository.HotelLoginRepository;
import com.example.personalassistant.repository.UserLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserLoginRepository userLoginRepository;

    @Autowired
    private HotelLoginRepository hotelLoginRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // USER LOGIN
        UserLogin user = userLoginRepository.findByEmail(email).orElse(null);
        if (user != null) {
            return User.withUsername(user.getEmail())
                    .password(user.getPassword())
                    .roles("USER")
                    .build();
        }

        // HOTEL LOGIN
        HotelLogin hotel = hotelLoginRepository.findByEmail(email).orElse(null);
        if (hotel != null) {
            return User.withUsername(hotel.getEmail())
                    .password(hotel.getPassword())
                    .roles("HOTEL")
                    .build();
        }

        throw new UsernameNotFoundException("User not found: " + email);
    }
}
