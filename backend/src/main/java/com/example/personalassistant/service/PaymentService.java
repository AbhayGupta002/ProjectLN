package com.example.personalassistant.service;

import com.example.personalassistant.entity.*;
import com.example.personalassistant.enums.BookingStatus;
import com.example.personalassistant.enums.PaymentStatus;
import com.example.personalassistant.dto.PaymentVerifyRequestDto;
import com.example.personalassistant.repository.*;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.codec.digest.HmacUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Value("${razorpay.key-id}")
    private String apiKey;

    @Value("${razorpay.key-secret}")
    private String apiSecret;

    @Autowired(required = false)
    private RazorpayClient razorpayClient;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TourBookingRepository tourBookingRepository;

    @Autowired
    private FlightBookingRepository flightBookingRepository;

    @Autowired
    private BusBookingRepository busBookingRepository;

    @Autowired
    private TrainBookingRepository trainBookingRepository;

    @Autowired
    private CabBookingRepository cabBookingRepository;

    @Autowired
    private PaymentPriceCacheService paymentPriceCacheService;

    private RazorpayClient getClient() throws Exception {
        if (razorpayClient != null) {
            return razorpayClient;
        }
        return new RazorpayClient(apiKey, apiSecret);
    }

    // Authoritative server-side order creation based on booking with TTL Price Cache
    @Transactional
    public Map<String, Object> createBookingOrder(String bookingType, Long bookingId, String userEmail) throws Exception {
        double serverAmount = paymentPriceCacheService.getAuthoritativePrice(bookingType, bookingId);

        // Check if pending order already exists
        Optional<Payment> existingPayment = paymentRepository.findByBookingTypeAndBookingId(bookingType.toUpperCase(), bookingId);
        if (existingPayment.isPresent() && existingPayment.get().getStatus() == PaymentStatus.PENDING) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("id", existingPayment.get().getRazorpayOrderId());
            resp.put("amount", existingPayment.get().getAmount().multiply(BigDecimal.valueOf(100)).intValue());
            resp.put("currency", "INR");
            resp.put("key", apiKey);
            return resp;
        }

        JSONObject options = new JSONObject();
        options.put("amount", (int) (serverAmount * 100));
        options.put("currency", "INR");
        options.put("receipt", "rcpt_" + bookingType.toLowerCase() + "_" + bookingId);

        Order order = getClient().orders.create(options);

        Payment payment = Payment.builder()
                .bookingType(bookingType.toUpperCase())
                .bookingId(bookingId)
                .userEmail(userEmail != null && !userEmail.isBlank() ? userEmail : "guest@worldtours.com")
                .description(bookingType.toUpperCase() + " Booking #" + bookingId)
                .razorpayOrderId(order.get("id"))
                .amount(BigDecimal.valueOf(serverAmount))
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        Map<String, Object> response = new HashMap<>();
        response.put("id", order.get("id"));
        response.put("amount", order.get("amount"));
        response.put("currency", order.get("currency"));
        response.put("key", apiKey);
        response.put("bookingType", bookingType);
        response.put("bookingId", bookingId);
        response.put("upiUri", "upi://pay?pa=worldtours@okaxis&pn=WorldTours&am=" + serverAmount + "&cu=INR&tr=" + order.get("id"));
        return response;
    }

    // Authoritative amount calculation directly from database
    private double getAuthoritativeAmount(String bookingType, Long bookingId) {
        return switch (bookingType.toUpperCase()) {
            case "HOTEL" -> bookingRepository.findById(bookingId)
                    .map(Booking::getAmount)
                    .orElseThrow(() -> new RuntimeException("Hotel booking not found: " + bookingId));
            case "TOUR" -> tourBookingRepository.findById(bookingId)
                    .map(TourBooking::getPrice)
                    .orElseThrow(() -> new RuntimeException("Tour booking not found: " + bookingId));
            case "FLIGHT" -> flightBookingRepository.findById(bookingId)
                    .map(FlightBooking::getTotalFare)
                    .orElseThrow(() -> new RuntimeException("Flight booking not found: " + bookingId));
            case "BUS" -> busBookingRepository.findById(bookingId)
                    .map(BusBooking::getTotalFare)
                    .orElseThrow(() -> new RuntimeException("Bus booking not found: " + bookingId));
            case "TRAIN" -> trainBookingRepository.findById(bookingId)
                    .map(TrainBooking::getTotalFare)
                    .orElseThrow(() -> new RuntimeException("Train booking not found: " + bookingId));
            case "CAB" -> cabBookingRepository.findById(bookingId)
                    .map(CabBooking::getEstimatedFare)
                    .orElseThrow(() -> new RuntimeException("Cab booking not found: " + bookingId));
            default -> throw new RuntimeException("Unsupported booking type: " + bookingType);
        };
    }

    // Legacy fallback order creation
    public Map<String, Object> createOrder(double amount) throws Exception {
        JSONObject options = new JSONObject();
        options.put("amount", (int) (amount * 100));
        options.put("currency", "INR");
        options.put("receipt", "txn_" + System.currentTimeMillis());

        Order order = getClient().orders.create(options);

        Map<String, Object> response = new HashMap<>();
        response.put("id", order.get("id"));
        response.put("amount", order.get("amount"));
        response.put("currency", order.get("currency"));
        response.put("key", apiKey);
        return response;
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String generatedSignature = HmacUtils.hmacSha256Hex(
                    apiSecret,
                    orderId + "|" + paymentId
            );
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean verifyAndSyncPayment(PaymentVerifyRequestDto request, String bookingType) {
        boolean isValid = verifySignature(request.getOrderId(), request.getPaymentId(), request.getSignature());

        Optional<Payment> paymentOpt = paymentRepository.findByRazorpayOrderId(request.getOrderId());
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setRazorpayPaymentId(request.getPaymentId());
            if (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank()) {
                payment.setPaymentMethod(request.getPaymentMethod());
            } else {
                payment.setPaymentMethod("UPI / ONLINE");
            }
            if (request.getUpiVpa() != null) {
                payment.setUpiVpa(request.getUpiVpa());
            }
            payment.setStatus(isValid ? PaymentStatus.PAID : PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }

        if (isValid && request.getBookingId() != null && bookingType != null) {
            updateBookingStatusOnPayment(bookingType, request.getBookingId());
        }

        return isValid;
    }

    public java.util.List<Payment> getUserTransactions(String userEmail, String status, String type, String search) {
        PaymentStatus paymentStatus = null;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            try {
                paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            } catch (Exception ignored) {}
        }
        String bookingType = (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type)) ? type.toUpperCase() : null;
        String searchTerm = (search != null && !search.isBlank()) ? search.trim() : null;

        return paymentRepository.searchUserTransactions(userEmail, paymentStatus, bookingType, searchTerm);
    }

    private void updateBookingStatusOnPayment(String bookingType, Long bookingId) {
        switch (bookingType.toUpperCase()) {
            case "HOTEL" -> bookingRepository.findById(bookingId).ifPresent(b -> {
                b.setPaymentStatus("PAID");
                b.setBookingStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(b);
            });
            case "TOUR" -> tourBookingRepository.findById(bookingId).ifPresent(b -> {
                b.setStatus(BookingStatus.CONFIRMED);
                tourBookingRepository.save(b);
            });
            case "FLIGHT" -> flightBookingRepository.findById(bookingId).ifPresent(b -> {
                b.setPaymentStatus(PaymentStatus.PAID);
                b.setBookingStatus(BookingStatus.CONFIRMED);
                flightBookingRepository.save(b);
            });
            case "BUS" -> busBookingRepository.findById(bookingId).ifPresent(b -> {
                b.setPaymentStatus(PaymentStatus.PAID);
                b.setBookingStatus(BookingStatus.CONFIRMED);
                busBookingRepository.save(b);
            });
            case "TRAIN" -> trainBookingRepository.findById(bookingId).ifPresent(b -> {
                b.setPaymentStatus(PaymentStatus.PAID);
                b.setBookingStatus(BookingStatus.CONFIRMED);
                trainBookingRepository.save(b);
            });
            case "CAB" -> cabBookingRepository.findById(bookingId).ifPresent(b -> {
                b.setPaymentStatus(PaymentStatus.PAID);
                b.setBookingStatus(BookingStatus.CONFIRMED);
                cabBookingRepository.save(b);
            });
        }
    }

    public double updateBookingPrice(String bookingType, Long bookingId, double newPrice) {
        return paymentPriceCacheService.updateDatabasePriceAndEvict(bookingType, bookingId, newPrice);
    }

    public void evictPriceCache(String bookingType, Long bookingId) {
        if (bookingType != null && bookingId != null) {
            paymentPriceCacheService.evictPrice(bookingType, bookingId);
        } else {
            paymentPriceCacheService.clearAll();
        }
    }
}
