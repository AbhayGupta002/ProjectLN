//package com.example.personalassistant.service;
//
//import com.razorpay.Order;
//import jakarta.transaction.Transactional;
//import lombok.extern.slf4j.Slf4j;
//import org.json.JSONObject;
//import com.razorpay.RazorpayClient;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.apache.commons.codec.digest.HmacUtils;
//import com.example.personalassistant.entity.Payment;
//import com.example.personalassistant.entity.TourBooking;
//import com.example.personalassistant.enums.PaymentStatus;
//import com.example.personalassistant.enums.BookingStatus;
//import com.example.personalassistant.dto.PaymentVerifyRequestDto;
//import com.example.personalassistant.repository.PaymentRepository;
//import com.example.personalassistant.repository.TourBookingRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.beans.factory.annotation.Autowired;
//
//@Service
//@Slf4j
//public class PaymentService {
//
//    @Value("${razorpay.key}")
//    private String apiKey;
//
//    @Value("${razorpay.secret}")
//    private String apiSecret;
//
//    @Autowired
//    private PaymentRepository paymentRepository;
//
//    @Autowired
//    private TourBookingRepository tourBookingRepository;
//
//    public Order createOrder(Long bookingId) throws Exception {
//
//        TourBooking booking = tourBookingRepository.findById(bookingId)
//                .orElseThrow(() -> new RuntimeException("Booking not found"));
//
//        RazorpayClient client = new RazorpayClient(apiKey, apiSecret);
//
//        JSONObject options = new JSONObject();
//        options.put("amount", booking.getPrice();
//        options.put("currency", "INR");
//        options.put("receipt", "booking_" + bookingId);
//
//        Order order = client.orders.create(options);
//
//        // Save payment as CREATED
//        Payment payment = Payment.builder()
//                .booking(booking)
//                .razorpayOrderId(order.get("id"))
//                .amount(booking.getPrice())
//                .status(PaymentStatus.CREATED)
//                .build();
//
//        paymentRepository.save(payment);
//
//        return order;
//    }
//
//    @Transactional
//    public ResponseEntity<?> verifyPayment(PaymentVerifyRequestDto request) {
//
//        try {
//
//            String generatedSignature = HmacUtils.hmacSha256Hex(
//                    apiSecret,
//                    request.getOrderId() + "|" + request.getPaymentId()
//            );
//
//            if (!generatedSignature.equals(request.getSignature())) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body("Invalid signature");
//            }
//
//            // Fetch existing payment by orderId
//            Payment payment = paymentRepository
//                    .findByRazorpayOrderId(request.getOrderId())
//                    .orElseThrow(() -> new RuntimeException("Payment not found"));
//
//            // Prevent double verification
//            if (payment.getStatus() == PaymentStatus.SUCCESS) {
//                return ResponseEntity.ok("Payment already verified");
//            }
//
//            // Update payment
//            payment.setRazorpayPaymentId(request.getPaymentId());
//            payment.setStatus(PaymentStatus.SUCCESS);
//
//            paymentRepository.save(payment);
//
//            // Confirm booking
//            TourBooking booking = payment.getBooking();
//            booking.setStatus(BookingStatus.CONFIRMED);
//            tourBookingRepository.save(booking);
//
//            return ResponseEntity.ok("Payment successful & booking confirmed");
//
//        } catch (Exception e) {
//            log.error("Payment verification error", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Payment verification error");
//        }
//    }
//}
