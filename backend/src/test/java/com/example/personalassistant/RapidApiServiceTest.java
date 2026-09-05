package com.example.personalassistant;

import com.example.personalassistant.entity.Flight;
import com.example.personalassistant.entity.Train;
import com.example.personalassistant.service.RapidApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RapidApiServiceTest {

    private RapidApiService rapidApiService;

    @BeforeEach
    void setUp() {
        rapidApiService = new RapidApiService();
        ReflectionTestUtils.setField(rapidApiService, "rapidApiKey", "");
        ReflectionTestUtils.setField(rapidApiService, "rapidApiEnabled", true);
        ReflectionTestUtils.setField(rapidApiService, "flightHost", "flight-fare-search.p.rapidapi.com");
        ReflectionTestUtils.setField(rapidApiService, "flightUrl", "https://flight-fare-search.p.rapidapi.com/v2/flights");
        ReflectionTestUtils.setField(rapidApiService, "trainHost", "irctc1.p.rapidapi.com");
        ReflectionTestUtils.setField(rapidApiService, "trainUrl", "https://irctc1.p.rapidapi.com/api/v3/trainBetweenStations");
    }

    @Test
    void testResolveIataCodes() {
        assertEquals("DEL", rapidApiService.resolveIataCode("Delhi"));
        assertEquals("DEL", rapidApiService.resolveIataCode("New Delhi"));
        assertEquals("BOM", rapidApiService.resolveIataCode("Mumbai"));
        assertEquals("GOI", rapidApiService.resolveIataCode("Goa"));
        assertEquals("BLR", rapidApiService.resolveIataCode("Bengaluru"));
        assertEquals("VNS", rapidApiService.resolveIataCode("Varanasi"));
        assertEquals("CCU", rapidApiService.resolveIataCode("Kolkata"));
        assertEquals("JFK", rapidApiService.resolveIataCode("JFK"));
    }

    @Test
    void testResolveStationCodes() {
        assertEquals("NDLS", rapidApiService.resolveStationCode("Delhi"));
        assertEquals("NDLS", rapidApiService.resolveStationCode("New Delhi"));
        assertEquals("BSB", rapidApiService.resolveStationCode("Varanasi"));
        assertEquals("CSMT", rapidApiService.resolveStationCode("Mumbai"));
        assertEquals("HWH", rapidApiService.resolveStationCode("Howrah"));
        assertEquals("SBC", rapidApiService.resolveStationCode("Bengaluru"));
        assertEquals("MAS", rapidApiService.resolveStationCode("Chennai"));
    }

    @Test
    void testFallbackWhenUnconfigured() {
        assertFalse(rapidApiService.isConfigured());

        List<Flight> flights = rapidApiService.searchFlightsFromRapidApi("Delhi", "Mumbai", "2026-09-10");
        assertNotNull(flights);
        assertTrue(flights.isEmpty(), "When unconfigured, rapidapi should return empty list to trigger DB fallback");

        List<Train> trains = rapidApiService.searchTrainsFromRapidApi("Delhi", "Varanasi", "2026-09-10");
        assertNotNull(trains);
        assertTrue(trains.isEmpty(), "When unconfigured, rapidapi should return empty list to trigger DB fallback");
    }

    @Test
    void testStatusMap() {
        Map<String, Object> status = rapidApiService.getStatus();
        assertNotNull(status);
        assertEquals(false, status.get("configured"));
        assertEquals(true, status.get("enabled"));
        assertEquals("flight-fare-search.p.rapidapi.com", status.get("flightHost"));
        assertEquals("irctc1.p.rapidapi.com", status.get("trainHost"));
    }
}
