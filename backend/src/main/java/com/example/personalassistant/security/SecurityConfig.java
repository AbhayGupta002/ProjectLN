package com.example.personalassistant.security;

import com.example.personalassistant.service.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(
            JwtFilter jwtFilter,
            CustomUserDetailsService customUserDetailsService
    ) {
        this.jwtFilter = jwtFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(Customizer.withDefaults())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // CORS preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // Root, health and error endpoints
                        .requestMatchers(
                                "/",
                                "/error",
                                "/api/health",
                                "/actuator/health"
                        )
                        .permitAll()

                        // Public authentication endpoints
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/send-registration-otp",
                                "/api/auth/login",
                                "/api/auth/hotelregister",
                                "/api/auth/hotellogin",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/verify-2fa",
                                "/api/admin/login",
                                "/api/admin/register",
                                "/api/otp/**",
                                "/api/rapidapi/**"
                        )
                        .permitAll()

                        // Public search and catalog endpoints
                        .requestMatchers(
                                HttpMethod.GET,
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
                                "/api/tour-booking/tourbylocation"
                        )
                        .permitAll()

                        // Public AI and chat endpoints
                        .requestMatchers(
                                "/api/chat/**",
                                "/api/ai/**"
                        )
                        .permitAll()

                        // Admin endpoints
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Hotel dashboard and tour management
                        .requestMatchers(
                                "/api/HotelLoginDashboard/**",
                                "/api/hotellogindashboard/**",
                                "/api/tours/create",
                                "/api/tours/update/**",
                                "/api/tours/delete/**",
                                "/api/tour-booking/pending/**"
                        )
                        .hasRole("HOTEL")

                        // User dashboard
                        .requestMatchers("/api/dashboard/**")
                        .hasRole("USER")

                        // Booking and payment endpoints
                        .requestMatchers(
                                "/api/bookings/**",
                                "/api/flight-bookings/**",
                                "/api/bus-bookings/**",
                                "/api/train-bookings/**",
                                "/api/cab-bookings/**",
                                "/api/tour-booking/**",
                                "/api/payment/**"
                        )
                        .hasAnyRole("USER", "HOTEL", "ADMIN")

                        // Remaining endpoints require authentication
                        .anyRequest()
                        .authenticated()
                )

                .authenticationProvider(authenticationProvider())

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }
}