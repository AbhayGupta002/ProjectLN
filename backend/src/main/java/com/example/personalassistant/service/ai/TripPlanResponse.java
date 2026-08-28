package com.example.personalassistant.service.ai;

import com.example.personalassistant.entity.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripPlanResponse {
    private String destination;
    private String origin;
    private Integer durationDays;
    private Double budgetProvided;
    private String currency;
    private boolean isBudgetRealistic;
    private String budgetAdvice;
    private Double estimatedTotalCost;
    private List<Flight> availableFlights;
    private List<Train> availableTrains;
    private List<Bus> availableBuses;
    private List<Cab> availableCabs;
    private List<Hotel> recommendedHotels;
    private List<TourPackage> recommendedTours;
    private List<TourGuide> recommendedGuides;
    private List<Destination> attractions;
    private List<DayPlan> itinerary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayPlan {
        private int day;
        private String title;
        private String morningActivity;
        private String afternoonActivity;
        private String eveningActivity;
        private String stayRecommendation;
    }
}
