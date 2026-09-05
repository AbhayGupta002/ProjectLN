package com.example.personalassistant.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.personalassistant.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api")
public class PasswordResetController {
    @Autowired
    private PasswordResetService resetService;

    @Autowired
    private com.example.personalassistant.service.UserService userService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String,String> body) {
        String identifier = body.get("identifier");
        if (identifier == null || identifier.isBlank()) {
            identifier = body.get("email");
        }
        if (identifier == null || identifier.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Incorrect Details"));
        }
        return userService.forgotPassword(identifier);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String,String> body) {
        String token = body.get("token");
        String password = body.get("password");
        if (token==null||password==null)
            return ResponseEntity.badRequest().body("token and password required");
        try {
            resetService.resetPassword(token,password);
            return ResponseEntity.ok("Password updated.");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        return ResponseEntity.ok(java.util.Map.of("valid", resetService.validateToken(token)));
    }
}
