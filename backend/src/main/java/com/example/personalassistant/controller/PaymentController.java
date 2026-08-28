package com.example.personalassistant.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.example.personalassistant.service.PaymentService;
import com.example.personalassistant.dto.PaymentVerifyRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/createOrder")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> payload) {
        try {
            double amount = Double.parseDouble(payload.get("amount").toString());
            Map<String, Object> orderDetails = paymentService.createOrder(amount);
            return ResponseEntity.ok(orderDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Order creation failed: " + e.getMessage());
        }
    }

    @PostMapping("/create-booking-order")
    public ResponseEntity<?> createBookingOrder(
            @RequestParam String bookingType,
            @RequestParam Long bookingId,
            org.springframework.security.core.Authentication authentication) {
        try {
            String email = authentication != null ? authentication.getName() : "user";
            Map<String, Object> orderDetails = paymentService.createBookingOrder(bookingType, bookingId, email);
            return ResponseEntity.ok(orderDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-booking")
    public ResponseEntity<?> verifyBooking(
            @RequestBody PaymentVerifyRequestDto request,
            @RequestParam(required = false, defaultValue = "HOTEL") String bookingType) {
        boolean isValid = paymentService.verifyAndSyncPayment(request, bookingType);
        if (isValid) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Payment verified and booking confirmed"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Invalid payment signature"));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerifyRequestDto request) {
        boolean isValid = paymentService.verifySignature(
                request.getOrderId(),
                request.getPaymentId(),
                request.getSignature()
        );
        if (isValid) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Payment verified successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Invalid payment signature"));
        }
    }
}
