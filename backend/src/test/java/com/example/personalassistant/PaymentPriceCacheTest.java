package com.example.personalassistant;

import com.example.personalassistant.entity.Booking;
import com.example.personalassistant.entity.TrainBooking;
import com.example.personalassistant.repository.BookingRepository;
import com.example.personalassistant.repository.TrainBookingRepository;
import com.example.personalassistant.service.PaymentPriceCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentPriceCacheTest {

    private PaymentPriceCacheService cacheService;
    private Booking testBooking;
    private TrainBooking testTrainBooking;
    private AtomicInteger bookingFindCalls;
    private AtomicInteger trainBookingSaveCalls;

    @BeforeEach
    public void setup() {
        cacheService = new PaymentPriceCacheService();
        cacheService.setCacheTtlMs(5000); // 5 second TTL
        cacheService.clearAll();

        testBooking = new Booking();
        testBooking.setId(101L);
        testBooking.setAmount(4500.0);

        testTrainBooking = new TrainBooking();
        testTrainBooking.setId(202L);
        testTrainBooking.setTotalFare(1750.0);

        bookingFindCalls = new AtomicInteger(0);
        trainBookingSaveCalls = new AtomicInteger(0);

        BookingRepository bookingRepo = (BookingRepository) Proxy.newProxyInstance(
                BookingRepository.class.getClassLoader(),
                new Class<?>[]{BookingRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        bookingFindCalls.incrementAndGet();
                        if (Long.valueOf(101L).equals(args[0])) return Optional.of(testBooking);
                        return Optional.empty();
                    }
                    if ("save".equals(method.getName())) return args[0];
                    return null;
                }
        );

        TrainBookingRepository trainRepo = (TrainBookingRepository) Proxy.newProxyInstance(
                TrainBookingRepository.class.getClassLoader(),
                new Class<?>[]{TrainBookingRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        if (Long.valueOf(202L).equals(args[0])) return Optional.of(testTrainBooking);
                        return Optional.empty();
                    }
                    if ("save".equals(method.getName())) {
                        trainBookingSaveCalls.incrementAndGet();
                        return args[0];
                    }
                    return null;
                }
        );

        ReflectionTestUtils.setField(cacheService, "bookingRepository", bookingRepo);
        ReflectionTestUtils.setField(cacheService, "trainBookingRepository", trainRepo);
    }

    @Test
    public void testCacheHitWithinTtl() {
        // 1st call -> Cache Miss, queries repository
        double price1 = cacheService.getAuthoritativePrice("HOTEL", 101L);
        assertEquals(4500.0, price1);
        assertEquals(1, bookingFindCalls.get());

        // 2nd call -> Cache Hit, must NOT query repository again
        double price2 = cacheService.getAuthoritativePrice("HOTEL", 101L);
        assertEquals(4500.0, price2);
        assertEquals(1, bookingFindCalls.get(), "Database should not be queried again within TTL");
        assertTrue(cacheService.isCached("HOTEL", 101L));
    }

    @Test
    public void testInstantCacheUpdateOnDatabasePriceChange() {
        // Load initial price in cache
        double initial = cacheService.getAuthoritativePrice("TRAIN", 202L);
        assertEquals(1750.0, initial);

        // Update database price -> Cache must be updated instantly
        double updated = cacheService.updateDatabasePriceAndEvict("TRAIN", 202L, 1950.0);
        assertEquals(1950.0, updated);
        assertEquals(1950.0, testTrainBooking.getTotalFare());
        assertEquals(1, trainBookingSaveCalls.get());

        // Next read returns 1950.0 immediately
        double fromCache = cacheService.getAuthoritativePrice("TRAIN", 202L);
        assertEquals(1950.0, fromCache);
    }

    @Test
    public void testCacheEviction() {
        cacheService.getAuthoritativePrice("HOTEL", 101L);
        assertTrue(cacheService.isCached("HOTEL", 101L));

        // Explicit eviction
        cacheService.evictPrice("HOTEL", 101L);
        assertFalse(cacheService.isCached("HOTEL", 101L));
    }

    @Test
    public void testCacheExpiryWithZeroTtl() throws InterruptedException {
        // Set TTL to 0 ms (immediate expiration)
        cacheService.setCacheTtlMs(0);

        cacheService.getAuthoritativePrice("HOTEL", 101L);
        assertEquals(1, bookingFindCalls.get());

        Thread.sleep(5);

        // Next call must see expired entry and query repository again
        cacheService.getAuthoritativePrice("HOTEL", 101L);
        assertEquals(2, bookingFindCalls.get(), "Expired entry should trigger reload from DB");
    }
}
