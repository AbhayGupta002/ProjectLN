package com.example.personalassistant.service;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.personalassistant.entity.Admin;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.dto.AdminDto;
import com.example.personalassistant.security.JwtUtil;
import com.example.personalassistant.dto.ErrorDetails;
import com.example.personalassistant.entity.AdminLogin;
import com.example.personalassistant.repository.AdminRepository;
import com.example.personalassistant.repository.HotelRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.personalassistant.repository.AdminLoginRepository;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;


@Service
public class AdminService {
    @Autowired
    private AdminLoginRepository adminLoginRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AdminRepository adminRepository;

    public ResponseEntity<Response> adminRegister(AdminDto adminDto){

        Response response = new Response();

        Optional<AdminLogin> existingAdmin =
                adminLoginRepository.findByEmail(adminDto.getEmail());

        if(existingAdmin.isPresent()){
            ErrorDetails errorDetails = new ErrorDetails(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
            response.setError(errorDetails);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        Admin admin = new Admin();
        admin.setName(adminDto.getName());
        admin.setNumber(adminDto.getNumber());
        admin.setEmail(adminDto.getEmail());

        Admin savedAdmin = adminRepository.save(admin);

        AdminLogin credential = new AdminLogin();
        credential.setEmail(adminDto.getEmail());
        credential.setPassword(passwordEncoder.encode(adminDto.getPassword()));

        adminLoginRepository.save(credential);

        response.setData(savedAdmin);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    public ResponseEntity<Response> adminLogin(String email, String password) {

        Response response = new Response();
        AdminLogin adminLogin = adminLoginRepository.findByEmail(email).orElse(null);  // in optional null doesn't work
        if (adminLogin == null) {                 /*in Optional<> null doesn't support....  */
            ErrorDetails error = new ErrorDetails(
                    HttpStatus.NOT_FOUND,
                    "User does not exist"
            );
            response.setError(error);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (!passwordEncoder.matches(password, adminLogin.getPassword())) {
            ErrorDetails error = new ErrorDetails(
                    HttpStatus.BAD_REQUEST,
                    "Invalid email or password"
            );
            response.setError(error);
            return ResponseEntity.badRequest().body(response);
        }

        String adminToken = jwtUtil.generateToken(email,"ADMIN");
        response.setData(adminToken);
        return ResponseEntity.ok(response);
    }
}
