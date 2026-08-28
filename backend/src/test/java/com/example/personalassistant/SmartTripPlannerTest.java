package com.example.personalassistant;

import com.example.personalassistant.repository.*;
import com.example.personalassistant.service.ai.SmartTripPlannerService;
import com.example.personalassistant.service.ai.TripPlanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SmartTripPlannerTest {

    private SmartTripPlannerService tripPlannerService;

    @SuppressWarnings("unchecked")
    private <T> T createEmptyProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                (proxy, method, args) -> {
                    if (method.getReturnType().equals(List.class)) return Collections.emptyList();
                    if (method.getReturnType().equals(Optional.class)) return Optional.empty();
                    if (method.getReturnType().equals(boolean.class)) return false;
                    return null;
                }
        );
    }

    @BeforeEach
    public void setup() {
        tripPlannerService = new SmartTripPlannerService();
        ReflectionTestUtils.setField(tripPlannerService, "flightRepository", createEmptyProxy(FlightRepository.class));
        ReflectionTestUtils.setField(tripPlannerService, "trainRepository", createEmptyProxy(TrainRepository.class));
        ReflectionTestUtils.setField(tripPlannerService, "busRepository", createEmptyProxy(BusRepository.class));
        ReflectionTestUtils.setField(tripPlannerService, "cabRepository", createEmptyProxy(CabRepository.class));
        ReflectionTestUtils.setField(tripPlannerService, "hotelRepository", createEmptyProxy(HotelRepository.class));
        ReflectionTestUtils.setField(tripPlannerService, "tourPackageRepository", createEmptyProxy(TourPackageRepository.class));
        ReflectionTestUtils.setField(tripPlannerService, "tourGuideRepository", createEmptyProxy(TourGuideRepository.class));
        ReflectionTestUtils.setField(tripPlannerService, "destinationRepository", createEmptyProxy(DestinationRepository.class));
    }

    @Test
    public void testRealisticBudgetPlanning() {
        TripPlanResponse plan = tripPlannerService.planTrip("Delhi", "Mumbai", 3, 25000.0, "INR");
        assertNotNull(plan);
        assertEquals("Delhi", plan.getDestination());
        assertEquals(3, plan.getDurationDays());
        assertTrue(plan.isBudgetRealistic(), "25000 INR should be realistic for 3 days in Delhi");
        assertEquals(3, plan.getItinerary().size());
    }

    @Test
    public void testUnrealisticBudgetPlanning() {
        // 30 days in London for $100 is completely unrealistic
        TripPlanResponse plan = tripPlannerService.planTrip("London", "New York", 30, 100.0, "USD");
        assertNotNull(plan);
        assertFalse(plan.isBudgetRealistic(), "100 USD for 30 days in London must be flagged as unrealistic");
        assertTrue(plan.getBudgetAdvice().contains("unrealistic"), "Advice should clearly mention budget is unrealistic");
    }
}
