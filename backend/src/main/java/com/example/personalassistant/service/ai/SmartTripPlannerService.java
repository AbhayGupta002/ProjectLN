package com.example.personalassistant.service.ai;

import com.example.personalassistant.entity.*;
import com.example.personalassistant.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SmartTripPlannerService {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private CabRepository cabRepository;

    @Autowired
    private TourPackageRepository tourPackageRepository;

    @Autowired
    private TourGuideRepository tourGuideRepository;

    @Autowired
    private DestinationRepository destinationRepository;

    public TripPlanResponse planTrip(String destination, String origin, Integer durationDays, Double budget, String currency) {
        final String dest = (destination == null || destination.isBlank()) ? "Delhi" : destination.trim();
        int days = (durationDays != null && durationDays > 0) ? durationDays : 3;
        String curr = (currency != null && !currency.isBlank()) ? currency.toUpperCase() : "INR";

        // 1. Fetch Real Transportation from DB
        List<Flight> flights = new ArrayList<>();
        List<Train> trains = new ArrayList<>();
        List<Bus> buses = new ArrayList<>();

        if (origin != null && !origin.isBlank()) {
            flights = flightRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(origin.trim(), dest);
            trains = trainRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(origin.trim(), dest);
            buses = busRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(origin.trim(), dest);
        } else {
            // General destination search
            flights = flightRepository.findByStatusTrue().stream()
                    .filter(f -> f.getDestination().equalsIgnoreCase(dest))
                    .limit(5).toList();
            trains = trainRepository.findByStatusTrue().stream()
                    .filter(t -> t.getDestination().equalsIgnoreCase(dest))
                    .limit(5).toList();
            buses = busRepository.findByStatusTrue().stream()
                    .filter(b -> b.getDestination().equalsIgnoreCase(dest))
                    .limit(5).toList();
        }

        // 2. Fetch Real Cabs in the destination
        List<Cab> cabs = cabRepository.findByCityIgnoreCaseAndAvailableTrue(dest);
        if (cabs.isEmpty()) {
            cabs = cabRepository.findByAvailableTrue().stream().limit(3).toList();
        }

        // 3. Fetch Real Active Hotels
        List<Hotel> hotels = hotelRepository.searchHotels(dest);
        if (hotels.isEmpty()) {
            hotels = hotelRepository.findTopHotels();
        }

        // 4. Fetch Real Tour Packages
        List<TourPackage> tours = tourPackageRepository.findByLocationFlexibleSorted(dest, "asc");
        if (tours.isEmpty()) {
            tours = tourPackageRepository.findAll().stream().limit(4).toList();
        }

        // 5. Fetch Real Tour Guides
        List<TourGuide> guides = tourGuideRepository.matchGuides(dest, null, null);
        if (guides.isEmpty()) {
            guides = tourGuideRepository.findByAvailableTrue().stream().limit(3).toList();
        }

        // 6. Fetch Tourist Attractions
        List<Destination> attractions = destinationRepository.findByDestinationNameContainingIgnoreCase(dest);
        if (attractions.isEmpty()) {
            attractions = destinationRepository.findByStatusTrue().stream().limit(4).toList();
        }

        // 7. Budget Feasibility Calculation
        double minDailyAccommodation = 1500.0; // INR
        double minDailyFoodTransport = 1000.0; // INR
        if ("USD".equalsIgnoreCase(curr)) {
            minDailyAccommodation = 60.0;
            minDailyFoodTransport = 40.0;
        } else if ("EUR".equalsIgnoreCase(curr)) {
            minDailyAccommodation = 55.0;
            minDailyFoodTransport = 35.0;
        } else if ("GBP".equalsIgnoreCase(curr)) {
            minDailyAccommodation = 50.0;
            minDailyFoodTransport = 35.0;
        }

        double minDailyRequired = minDailyAccommodation + minDailyFoodTransport;
        double estimatedTotalCost = (minDailyRequired * days);

        // Add transit cost if available
        if (!flights.isEmpty()) {
            estimatedTotalCost += flights.get(0).getFare();
        } else if (!trains.isEmpty()) {
            estimatedTotalCost += trains.get(0).getFare();
        }

        boolean isRealistic = true;
        String advice = "Budget looks great for a comfortable stay and activities!";

        if (budget != null && budget > 0) {
            if (budget < estimatedTotalCost * 0.6) {
                isRealistic = false;
                advice = String.format(
                        "Your proposed budget of %s %.0f for %d days in %s is unrealistic (average realistic minimum is %s %.0f). " +
                        "We recommend: 1) Shortening the trip to %d days, 2) Staying in hostel dorms/homestays, or 3) Considering budget-friendly alternate locations.",
                        curr, budget, days, dest, curr, estimatedTotalCost,
                        Math.max(1, (int)(budget / minDailyRequired))
                );
            } else if (budget < estimatedTotalCost) {
                advice = String.format("Tight budget! Consider opting for trains/buses instead of flights and economy rooms.");
            }
        }

        // 8. Generate Day-by-Day Practical Itinerary
        List<TripPlanResponse.DayPlan> itinerary = new ArrayList<>();
        String hotelName = !hotels.isEmpty() ? hotels.get(0).getHotel() : "Comfort Inn " + dest;

        for (int i = 1; i <= Math.min(days, 10); i++) {
            if (i == 1) {
                itinerary.add(TripPlanResponse.DayPlan.builder()
                        .day(1)
                        .title("Arrival & Check-in")
                        .morningActivity("Arrival at " + dest + ", airport/station pickup via cab or local transit.")
                        .afternoonActivity("Check-in at " + hotelName + " and refresh.")
                        .eveningActivity("Evening stroll around local markets and welcoming dinner.")
                        .stayRecommendation(hotelName)
                        .build());
            } else if (i == days) {
                itinerary.add(TripPlanResponse.DayPlan.builder()
                        .day(i)
                        .title("Wrap-up & Departure")
                        .morningActivity("Breakfast at hotel, souvenir shopping and packing.")
                        .afternoonActivity("Check-out and transit to airport/station.")
                        .eveningActivity("Safe return journey home.")
                        .stayRecommendation("Departure")
                        .build());
            } else {
                String sight = (!attractions.isEmpty() && attractions.size() >= i)
                        ? attractions.get(i - 1).getDestinationName()
                        : "Historic highlights and city viewpoints";
                itinerary.add(TripPlanResponse.DayPlan.builder()
                        .day(i)
                        .title("Day " + i + " Sightseeing & Exploration")
                        .morningActivity("Guided tour of " + sight + " with certified local guide.")
                        .afternoonActivity("Lunch with authentic local cuisines, visit local arts & museums.")
                        .eveningActivity("Sunset viewpoint and cultural performances.")
                        .stayRecommendation(hotelName)
                        .build());
            }
        }

        return TripPlanResponse.builder()
                .destination(dest)
                .origin(origin)
                .durationDays(days)
                .budgetProvided(budget)
                .currency(curr)
                .isBudgetRealistic(isRealistic)
                .budgetAdvice(advice)
                .estimatedTotalCost(estimatedTotalCost)
                .availableFlights(flights)
                .availableTrains(trains)
                .availableBuses(buses)
                .availableCabs(cabs)
                .recommendedHotels(hotels)
                .recommendedTours(tours)
                .recommendedGuides(guides)
                .attractions(attractions)
                .itinerary(itinerary)
                .build();
    }
}
