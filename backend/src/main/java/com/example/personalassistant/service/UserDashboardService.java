package com.example.personalassistant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import com.example.personalassistant.dto.*;
import org.springframework.http.ResponseEntity;
import com.example.personalassistant.entity.User;
import com.example.personalassistant.security.JwtUtil;
import com.example.personalassistant.entity.UserLogin;
import com.example.personalassistant.entity.Complaint;
import com.example.personalassistant.enums.AccountEnum;
import com.example.personalassistant.repository.UserRepository;
import com.example.personalassistant.repository.UserLoginRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.personalassistant.repository.ComplaintRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class UserDashboardService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserLoginRepository userLoginRepository;

    @Autowired
    private HotelDashboardService hotelDashboardService;


    public User getUserProfile(String token) {
        String email = jwtUtil.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return user;
    }

    public User updateProfile(String email, UpdateProfileRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        user.setName(request.getName());
        user.setMobile(request.getMobile());
        user.setCity(request.getCity());
        return userRepository.save(user);
    }



        public Complaint addComplaint(ComplaintRequest req) {
            Complaint c = new Complaint();
            c.setUserId(req.getUserId());
            c.setBookingId(req.getBookingId());
            c.setMessage(req.getMessage());
            return complaintRepository.save(c);
        }

    public ResponseEntity<?> disableAccount(String email, String password) {

        Response response= new Response();

        Optional<UserLogin> loginOptional =
                userLoginRepository.findByEmail(email);
        if (loginOptional.isEmpty()) {
            ErrorDetails errorDetails= new ErrorDetails(HttpStatus.BAD_REQUEST
            ,"User not found:"+email);
            response.setError(errorDetails);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        UserLogin login = loginOptional.get();

        // Check password
        if (!passwordEncoder.matches(password, login.getPassword())) {
            ErrorDetails errorDetails= new ErrorDetails(HttpStatus.NOT_FOUND
                    ,"Incorrect id or password"+email);
            response.setError(errorDetails);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // 🔥 Disable login
        User user = new User();
        user.setAccountEnum(AccountEnum.INACTIVE);
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok("Account disabled successfully");
    }

        public List<Complaint> getUserComplaints(Long userId) {
            return complaintRepository.findByUserId(userId);
        }

        public List<Complaint> getAllComplaints() {
            return complaintRepository.findAll();
        }



}
