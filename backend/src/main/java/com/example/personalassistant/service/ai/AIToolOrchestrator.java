package com.example.personalassistant.service.ai;

import com.example.personalassistant.entity.*;
import com.example.personalassistant.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIToolOrchestrator {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private TourPackageRepository tourPackageRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private CabRepository cabRepository;

    @Autowired
    private TourGuideRepository tourGuideRepository;

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
    private UserRepository userRepository;

    @Autowired
    private SmartTripPlannerService tripPlannerService;

    public List<Hotel> searchHotels(String query) {
        if (query == null || query.isBlank()) {
            return hotelRepository.findTopHotels();
        }
        return hotelRepository.searchHotels(query.trim());
    }

    public List<TourPackage> searchTours(String location, String sort) {
        if (location == null || location.isBlank()) {
            return tourPackageRepository.findAll().stream().limit(10).toList();
        }
        return tourPackageRepository.findByLocationFlexibleSorted(location.trim(), sort != null ? sort : "asc");
    }

    public List<Flight> searchFlights(String source, String destination) {
        if (source != null && destination != null) {
            return flightRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(source.trim(), destination.trim());
        } else if (destination != null) {
            return flightRepository.findByStatusTrue().stream()
                    .filter(f -> f.getDestination().equalsIgnoreCase(destination.trim()))
                    .toList();
        }
        return flightRepository.findByStatusTrue();
    }

    public List<Train> searchTrains(String source, String destination) {
        if (source != null && destination != null) {
            return trainRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(source.trim(), destination.trim());
        } else if (destination != null) {
            return trainRepository.findByStatusTrue().stream()
                    .filter(t -> t.getDestination().equalsIgnoreCase(destination.trim()))
                    .toList();
        }
        return trainRepository.findByStatusTrue();
    }

    public List<Bus> searchBuses(String source, String destination) {
        if (source != null && destination != null) {
            return busRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(source.trim(), destination.trim());
        } else if (destination != null) {
            return busRepository.findByStatusTrue().stream()
                    .filter(b -> b.getDestination().equalsIgnoreCase(destination.trim()))
                    .toList();
        }
        return busRepository.findByStatusTrue();
    }

    public List<Cab> searchCabs(String city) {
        if (city == null || city.isBlank()) {
            return cabRepository.findByAvailableTrue();
        }
        return cabRepository.findByCityIgnoreCaseAndAvailableTrue(city.trim());
    }

    public List<TourGuide> findGuides(String city, String language, Double maxPrice) {
        return tourGuideRepository.matchGuides(city, language, maxPrice);
    }

    public Map<String, Object> getUserBookings(String userEmail) {
        Map<String, Object> history = new HashMap<>();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            history.put("error", "User not found");
            return history;
        }

        history.put("hotels", bookingRepository.findByUserEmail(userEmail));
        history.put("tours", tourBookingRepository.findByUserId(user.getId()));
        history.put("flights", flightBookingRepository.findByUserId(user.getId()));
        history.put("buses", busBookingRepository.findByUserId(user.getId()));
        history.put("trains", trainBookingRepository.findByUserId(user.getId()));
        history.put("cabs", cabBookingRepository.findByUserId(user.getId()));
        return history;
    }

    public TripPlanResponse planTrip(String destination, String origin, Integer days, Double budget, String currency) {
        return tripPlannerService.planTrip(destination, origin, days, budget, currency);
    }
}
