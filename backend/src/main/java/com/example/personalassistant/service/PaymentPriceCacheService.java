package com.example.personalassistant.service;

import com.example.personalassistant.entity.*;
import com.example.personalassistant.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance, thread-safe Payment Price Cache with configurable TTL
 * and instant cache update / eviction upon database price changes.
 */
@Service
public class PaymentPriceCacheService {

    private static final Logger log = LoggerFactory.getLogger(PaymentPriceCacheService.class);

    // Default TTL: 10 minutes (600,000 milliseconds)
    @Value("${payment.cache.ttl-ms:600000}")
    private long cacheTtlMs;

    public static class CacheEntry {
        private final double price;
        private final long expiresAt;

        public CacheEntry(double price, long ttlMs) {
            this.price = price;
            this.expiresAt = System.currentTimeMillis() + ttlMs;
        }

        public double getPrice() {
            return price;
        }

        public long getExpiresAt() {
            return expiresAt;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() >= expiresAt;
        }
    }

    private final Map<String, CacheEntry> priceCache = new ConcurrentHashMap<>();

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

    public String buildKey(String bookingType, Long bookingId) {
        return (bookingType != null ? bookingType.toUpperCase() : "GENERAL") + ":" + bookingId;
    }

    public void setCacheTtlMs(long cacheTtlMs) {
        this.cacheTtlMs = cacheTtlMs;
    }

    public long getCacheTtlMs() {
        return cacheTtlMs;
    }

    /**
     * Retrieve authoritative price from cache if valid; otherwise fetch from database and cache.
     */
    public double getAuthoritativePrice(String bookingType, Long bookingId) {
        String key = buildKey(bookingType, bookingId);
        CacheEntry entry = priceCache.get(key);

        if (entry != null && !entry.isExpired()) {
            log.info("⚡ [PAYMENT-CACHE HIT] Key: {} -> ₹{}", key, entry.getPrice());
            return entry.getPrice();
        }

        // Cache miss or expired: fetch authoritative price directly from database
        double dbPrice = loadPriceFromDatabase(bookingType, bookingId);
        priceCache.put(key, new CacheEntry(dbPrice, cacheTtlMs));
        log.info("🔄 [PAYMENT-CACHE REFRESH] Key: {} -> ₹{} (TTL: {} ms)", key, dbPrice, cacheTtlMs);
        return dbPrice;
    }

    /**
     * Instantly updates the database price AND immediately updates the cache with zero lag.
     */
    @Transactional
    public double updateDatabasePriceAndEvict(String bookingType, Long bookingId, double newPrice) {
        // 1. Update in database
        updateDatabaseRecord(bookingType, bookingId, newPrice);

        // 2. Instantly update cache with new price and fresh TTL
        String key = buildKey(bookingType, bookingId);
        priceCache.put(key, new CacheEntry(newPrice, cacheTtlMs));
        log.info("🚀 [PAYMENT-CACHE INSTANT UPDATE] Key: {} updated to ₹{}", key, newPrice);
        return newPrice;
    }

    /**
     * Instantly evict a booking price from cache.
     */
    public void evictPrice(String bookingType, Long bookingId) {
        String key = buildKey(bookingType, bookingId);
        priceCache.remove(key);
        log.info("🗑️ [PAYMENT-CACHE EVICT] Key: {} removed from cache", key);
    }

    /**
     * Clear all cached payment prices.
     */
    public void clearAll() {
        priceCache.clear();
        log.info("🧹 [PAYMENT-CACHE FLUSH] All cached prices cleared.");
    }

    public int getCacheSize() {
        return priceCache.size();
    }

    public boolean isCached(String bookingType, Long bookingId) {
        String key = buildKey(bookingType, bookingId);
        CacheEntry entry = priceCache.get(key);
        return entry != null && !entry.isExpired();
    }

    private double loadPriceFromDatabase(String bookingType, Long bookingId) {
        String type = (bookingType != null) ? bookingType.toUpperCase() : "";
        return switch (type) {
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

    private void updateDatabaseRecord(String bookingType, Long bookingId, double newPrice) {
        String type = (bookingType != null) ? bookingType.toUpperCase() : "";
        switch (type) {
            case "HOTEL" -> {
                Booking booking = bookingRepository.findById(bookingId)
                        .orElseThrow(() -> new RuntimeException("Hotel booking not found: " + bookingId));
                booking.setAmount(newPrice);
                bookingRepository.save(booking);
            }
            case "TOUR" -> {
                TourBooking tourBooking = tourBookingRepository.findById(bookingId)
                        .orElseThrow(() -> new RuntimeException("Tour booking not found: " + bookingId));
                tourBooking.setPrice(newPrice);
                tourBookingRepository.save(tourBooking);
            }
            case "FLIGHT" -> {
                FlightBooking flightBooking = flightBookingRepository.findById(bookingId)
                        .orElseThrow(() -> new RuntimeException("Flight booking not found: " + bookingId));
                flightBooking.setTotalFare(newPrice);
                flightBookingRepository.save(flightBooking);
            }
            case "BUS" -> {
                BusBooking busBooking = busBookingRepository.findById(bookingId)
                        .orElseThrow(() -> new RuntimeException("Bus booking not found: " + bookingId));
                busBooking.setTotalFare(newPrice);
                busBookingRepository.save(busBooking);
            }
            case "TRAIN" -> {
                TrainBooking trainBooking = trainBookingRepository.findById(bookingId)
                        .orElseThrow(() -> new RuntimeException("Train booking not found: " + bookingId));
                trainBooking.setTotalFare(newPrice);
                trainBookingRepository.save(trainBooking);
            }
            case "CAB" -> {
                CabBooking cabBooking = cabBookingRepository.findById(bookingId)
                        .orElseThrow(() -> new RuntimeException("Cab booking not found: " + bookingId));
                cabBooking.setEstimatedFare(newPrice);
                cabBookingRepository.save(cabBooking);
            }
            default -> throw new RuntimeException("Unsupported booking type for price update: " + bookingType);
        }
    }
}
