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

                        // -------------------- PUBLIC AUTH ENDPOINTS --------------------
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/hotelregister",
                                "/api/auth/hotellogin",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/flights/**",
                                "/api/flight-bookings/**",
                                "/api/bus/**",
                                "/api/bus-bookings/**",
                                "/api/trains/**",
                                "/api/train-bookings/**",
                                "/api/payment/**"
                        ).permitAll()

                        .requestMatchers("/api/chat/**").permitAll()

                        // -------------------- PUBLIC TOUR PACKAGE APIs --------------------WIP
                                // Public APIs
                                .requestMatchers("/api/tours",
                                        "/api/tours/all",
                                        "/api/tours/{id}").permitAll()

                                // Protected APIs
                                .requestMatchers("/api/tours/create",
                                        "/api/tours/update/**",
                                        "/api/tours/delete/**").hasRole("HOTEL")


                                // -------------------- TOUR BOOKING APIS (User Auth Required) --------------------
                        .requestMatchers(
                                "/api/tour-booking/create",
                                "/api/tour-booking/user/**",
                                "/api/tour-booking/cancel/**",
                                "/api/tour-booking/tourbylocation"
                        ).permitAll()

//                        BookingController APis
                                .requestMatchers("/api/bookings/bookhotel").hasAnyRole("USER","ADMIN","HOTEL")
                                .requestMatchers("/api/bookings/getallbookingbyhotel").hasAnyRole("HOTEL","ADMIN")
                                .requestMatchers("/api/bookings/gettourbooking").hasAnyRole("USER","ADMIN","HOTEL")
                                .requestMatchers("/api/bookings/getuserbookings").hasAnyRole("USER","ADMIN","HOTEL")
                                .requestMatchers("/api/bookings/**").hasRole("ADMIN")


                                // -------------------- USER DASHBOARD (ROLE_USER) --------------------
                        .requestMatchers("/api/dashboard/**").hasRole("USER")
                                .requestMatchers("/api/dashboard/get-active-hotel").hasRole("USER")
                                .requestMatchers("/api/dashboard/disable-account").hasRole("USER")

//                        AI MODULE ADD OLLAMA3
                                .requestMatchers("/api/ai/prompt").permitAll()

                        // -------------------- HOTEL DASHBOARD (ROLE_HOTEL) --------------------
                        .requestMatchers("/api/HotelLoginDashboard/**").hasRole("HOTEL")
                                .requestMatchers(
                                        "/api/tour-booking/pending/**"
                                ).hasRole("HOTEL")
                                .requestMatchers("/api/hotellogindashboard/update-profile").hasRole("HOTEL")
                                .requestMatchers("/api/tours/create").hasRole("HOTEL")

//                                .requestMatchers("api/payment/**").permitAll() //make this update

//                        @Admin section
                                .requestMatchers("/api/admin/login", "/api/admin/register").permitAll() // public endpoints
                                .requestMatchers("/api/admin").hasRole("ADMIN")
                                .requestMatchers("/api/admin/**").permitAll()
                                .requestMatchers("/api/admin/hotels/inactive").hasRole("ADMIN")
                                .requestMatchers("/api/admin/all-users").hasRole("ADMIN")
                                .requestMatchers("/api/admin/active-hotels").hasRole("ADMIN")
                                .requestMatchers("/api/admin/hotels").hasRole("ADMIN")
                                .requestMatchers("/api/admin/search").hasRole("ADMIN")
                                .requestMatchers("/api/admin/suspend-hotel/id").hasRole("ADMIN")
                                .requestMatchers("/api/admin/suspend-user/id").hasRole("ADMIN")
                                .requestMatchers("/api/admin/search").hasRole("ADMIN")
                                .requestMatchers("/api/admin/prompts").hasRole("ADMIN")

//                        @🔥 PUBLIC APIs
                                .requestMatchers("/api/public/**").permitAll() // 🔥 PUBLIC APIs


//                           @AI AUTOMATED MODULE
                                .requestMatchers("/api/ai/**").permitAll()
//                                .requestMatchers("/api/ai/**").permitAll()


                        //new things
                                .requestMatchers("/api/destination/**").permitAll()


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



