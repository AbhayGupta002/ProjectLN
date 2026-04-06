//package com.example.personalassistant.controller;
//
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//import com.example.personalassistant.service.PaymentService;
//import com.example.personalassistant.dto.PaymentVerifyRequestDto;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.beans.factory.annotation.Autowired;
//
//@RestController
//@RequestMapping("/api/payment")
//public class PaymentController {
//
//    @Autowired
//    private PaymentService paymentService;
//
//    @Value("${razorpay.key.secret}")
//    private String razorpayKeySecret;
//
//    @PostMapping("/create-order/{bookingId}")
//    public ResponseEntity<?> createOrder(@PathVariable Long bookingId) {
//        try {
//            var order = paymentService.createOrder(bookingId);
//
//            return ResponseEntity.ok().body(
//                    java.util.Map.of(
//                            "orderId", order.get("id"),
//                            "amount", order.get("amount"),
//                            "currency", order.get("currency")
//                    )
//            );
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Order creation failed: " + e.getMessage());
//        }
//    }
//
//
//    @PostMapping("/verify")
//    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerifyRequestDto request) {
//        return paymentService.verifyPayment(request);
//    }
//}
