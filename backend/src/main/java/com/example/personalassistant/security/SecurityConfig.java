package com.example.personalassistant.security;


import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import com.example.personalassistant.service.CustomUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(JwtFilter jwtFilter, CustomUserDetailsService customUserDetailsService) {
        this.jwtFilter = jwtFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // -------------------- PUBLIC AUTH & DISCOVERY --------------------
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/hotelregister",
                                "/api/auth/hotellogin",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/verify-2fa",
                                "/api/admin/login",
                                "/actuator/**",
                                "/error"
                        ).permitAll()

                        // -------------------- PUBLIC CATALOGS & SEARCH --------------------
                        .requestMatchers(
                                "/api/public/**",
                                "/api/destination",
                                "/api/destination/**",
                                "/api/flights",
                                "/api/flights/**",
                                "/api/bus",
                                "/api/bus/**",
                                "/api/trains",
                                "/api/trains/**",
                                "/api/cabs",
                                "/api/cabs/**",
                                "/api/tours",
                                "/api/tours/**",
                                "/api/tour-booking/tourbylocation",
                                "/api/chat/**",
                                "/api/ai/**"
                        ).permitAll()

                        // -------------------- ADMIN SECTION --------------------
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // -------------------- HOTEL MANAGEMENT SECTION --------------------
                        .requestMatchers(
                                "/api/HotelLoginDashboard/**",
                                "/api/hotellogindashboard/**",
                                "/api/tours/create",
                                "/api/tours/update/**",
                                "/api/tours/delete/**",
                                "/api/tour-booking/pending/**"
                        ).hasRole("HOTEL")

                        // -------------------- USER CLIENT DASHBOARD --------------------
                        .requestMatchers("/api/dashboard/**").hasRole("USER")

                        // -------------------- SECURE BOOKING & PAYMENT TRANSACTIONS --------------------
                        .requestMatchers(
                                "/api/bookings/**",
                                "/api/flight-bookings/**",
                                "/api/bus-bookings/**",
                                "/api/train-bookings/**",
                                "/api/cab-bookings/**",
                                "/api/tour-booking/**",
                                "/api/payment/**"
                        ).hasAnyRole("USER", "HOTEL", "ADMIN")

                        // -------------------- ANY OTHER REQUEST → AUTH REQUIRED --------------------
                        .anyRequest().authenticated()
                )

                .authenticationProvider(authProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}



