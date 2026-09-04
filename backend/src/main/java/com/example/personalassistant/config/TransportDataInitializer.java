package com.example.personalassistant.config;

import com.example.personalassistant.entity.Bus;
import com.example.personalassistant.entity.Flight;
import com.example.personalassistant.entity.Train;
import com.example.personalassistant.repository.BusRepository;
import com.example.personalassistant.repository.FlightRepository;
import com.example.personalassistant.repository.TrainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Component
@Order(2)
public class TransportDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TransportDataInitializer.class);

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private BusRepository busRepository;

    @Override
    public void run(String... args) {
        seedTrains();
        seedFlights();
        seedBuses();
    }

    private void seedTrains() {
        if (trainRepository.count() > 0) {
            log.info("Trains already seeded ({} records found).", trainRepository.count());
            return;
        }

        List<Train> trains = List.of(
                createTrain("22436", "Vande Bharat Express", "New Delhi", "Varanasi", LocalTime.of(6, 0), LocalTime.of(14, 0), 120, 78, 1750.0, "Executive Chair (EC), AC Chair (CC)", "Wi-Fi, Gourmet Hot Meals, Panoramic Windows, CCTV", "https://images.unsplash.com/photo-1474487548417-781cb71495f3?w=800"),
                createTrain("22439", "Vande Bharat Express", "New Delhi", "Katra", LocalTime.of(6, 0), LocalTime.of(14, 0), 120, 92, 1630.0, "Executive Chair (EC), AC Chair (CC)", "High-Speed Wi-Fi, Breakfast & Lunch, GPS Tracking", "https://images.unsplash.com/photo-1532105956626-9569c03602f6?w=800"),
                createTrain("20901", "Vande Bharat Express", "Mumbai Central", "Gandhinagar", LocalTime.of(6, 10), LocalTime.of(12, 25), 120, 64, 1420.0, "Executive Chair (EC), AC Chair (CC)", "Bio-Vacuum Toilets, Charging Ports, Infotainment", "https://images.unsplash.com/photo-1515165562839-978bbcf18277?w=800"),
                createTrain("82902", "Mumbai Tejas Express", "Ahmedabad", "Mumbai Central", LocalTime.of(6, 40), LocalTime.of(13, 5), 140, 110, 1550.0, "Executive Class, AC Chair Car", "On-board Hospitality, Smart Windows, CCTV", "https://images.unsplash.com/photo-1541427468627-a89a96e5ca1d?w=800"),
                createTrain("22692", "Bengaluru Rajdhani Express", "New Delhi", "Bengaluru", LocalTime.of(20, 45), LocalTime.of(5, 20), 200, 145, 3890.0, "1st AC, 2nd AC, 3rd AC", "Complimentary Bedroll, 3-Course Dinner, Breakfast", "https://images.unsplash.com/photo-1506015391300-4802dc74de2e?w=800"),
                createTrain("12952", "Mumbai Rajdhani Express", "New Delhi", "Mumbai Central", LocalTime.of(16, 55), LocalTime.of(8, 35), 180, 120, 3250.0, "1st AC, 2nd AC, 3rd AC", "Luxury Dining, Evening Snacks, Clean Bedding", "https://images.unsplash.com/photo-1474487548417-781cb71495f3?w=800"),
                createTrain("12302", "Howrah Rajdhani Express", "New Delhi", "Kolkata", LocalTime.of(16, 50), LocalTime.of(9, 55), 160, 98, 3410.0, "1st AC, 2nd AC, 3rd AC", "Full Pantry Service, Sanitized Linen, Rapid Transit", "https://images.unsplash.com/photo-1532105956626-9569c03602f6?w=800"),
                createTrain("12780", "Goa Express", "New Delhi", "Goa", LocalTime.of(15, 15), LocalTime.of(5, 40), 150, 85, 2450.0, "2nd AC, 3rd AC, Sleeper", "Pantry Car, Charging Points, Scenic Western Ghats Views", "https://images.unsplash.com/photo-1515165562839-978bbcf18277?w=800"),
                createTrain("12270", "Chennai Central Duronto", "New Delhi", "Chennai", LocalTime.of(15, 55), LocalTime.of(20, 10), 160, 60, 3100.0, "1st AC, 2nd AC, 3rd AC", "Non-stop Point-to-Point, Meal Included, Security Guards", "https://images.unsplash.com/photo-1541427468627-a89a96e5ca1d?w=800"),
                createTrain("12002", "Bhopal Shatabdi Express", "New Delhi", "Bhopal", LocalTime.of(6, 0), LocalTime.of(14, 40), 140, 130, 1490.0, "Executive Chair, AC Chair", "Fastest Day Train, Morning Tea, Fresh Breakfast, Water Bottles", "https://images.unsplash.com/photo-1506015391300-4802dc74de2e?w=800")
        );

        trainRepository.saveAll(trains);
        log.info("✅ Successfully seeded {} real-time trains.", trains.size());
    }

    private Train createTrain(String number, String name, String src, String dst, LocalTime dep, LocalTime arr, int total, int avail, double fare, String cls, String amenities, String img) {
        Train t = new Train();
        t.setTrainNumber(number);
        t.setTrainName(name);
        t.setSource(src);
        t.setDestination(dst);
        t.setDepartureTime(dep);
        t.setArrivalTime(arr);
        t.setTotalSeats(total);
        t.setAvailableSeats(avail);
        t.setFare(fare);
        t.setTrainClass(cls);
        t.setAmenities(amenities);
        t.setImageUrl(img);
        t.setStatus(true);
        return t;
    }

    private void seedFlights() {
        if (flightRepository.count() > 0) {
            log.info("Flights already seeded ({} records found).", flightRepository.count());
            return;
        }

        List<Flight> flights = List.of(
                createFlight("6E-205", "IndiGo", "Delhi", "Mumbai", LocalTime.of(7, 0), LocalTime.of(9, 15), 180, 42, 4850.0, "Economy", "Free Carry-On 7kg, In-flight Snacks, USB Charging", "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800"),
                createFlight("AI-887", "Air India", "Mumbai", "Goa", LocalTime.of(11, 30), LocalTime.of(12, 45), 150, 28, 3400.0, "Economy / Business", "Complimentary Hot Meal, Extra Legroom, 25kg Baggage", "https://images.unsplash.com/photo-1540339832862-474599807836?w=800"),
                createFlight("UK-819", "Vistara", "Delhi", "Bengaluru", LocalTime.of(9, 40), LocalTime.of(12, 25), 160, 35, 5600.0, "Premium Economy", "Star Alliance Gold Benefits, Starbucks Coffee, Live TV", "https://images.unsplash.com/photo-1520437358207-323b43b50729?w=800"),
                createFlight("SG-143", "SpiceJet", "Delhi", "Srinagar", LocalTime.of(8, 15), LocalTime.of(9, 45), 170, 19, 4100.0, "Economy", "SpiceMax Available, Himalayan Aerial Views", "https://images.unsplash.com/photo-1506015391300-4802dc74de2e?w=800"),
                createFlight("6E-512", "IndiGo", "Bengaluru", "Goa", LocalTime.of(14, 10), LocalTime.of(15, 20), 180, 50, 2950.0, "Economy", "Non-stop Rapid Connection, Quick Boarding", "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800"),
                createFlight("AI-441", "Air India", "Delhi", "Jaipur", LocalTime.of(16, 20), LocalTime.of(17, 15), 140, 30, 2600.0, "Economy", "Short Hop Shuttle, Refreshments Included", "https://images.unsplash.com/photo-1540339832862-474599807836?w=800")
        );

        flightRepository.saveAll(flights);
        log.info("✅ Successfully seeded {} domestic flights.", flights.size());
    }

    private Flight createFlight(String number, String airline, String src, String dst, LocalTime dep, LocalTime arr, int total, int avail, double fare, String cls, String amenities, String img) {
        Flight f = new Flight();
        f.setFlightNumber(number);
        f.setAirline(airline);
        f.setSource(src);
        f.setDestination(dst);
        f.setDepartureTime(dep);
        f.setArrivalTime(arr);
        f.setTotalSeats(total);
        f.setAvailableSeats(avail);
        f.setFare(fare);
        f.setFlightClass(cls);
        f.setAmenities(amenities);
        f.setImageUrl(img);
        f.setStatus(true);
        return f;
    }

    private void seedBuses() {
        if (busRepository.count() > 0) {
            log.info("Buses already seeded ({} records found).", busRepository.count());
            return;
        }

        List<Bus> buses = List.of(
                createBus("Zingbus Maxx Electric", "ZB-901", "Zingbus", "Delhi", "Manali", LocalTime.of(20, 0), LocalTime.of(8, 30), 36, 22, 1250.0, "AC Sleeper 2+1", "Personal LCD Screen, Blankets, USB Ports, Live GPS", "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=800"),
                createBus("Intrcity SmartBus Volvo Multi-Axle", "IC-402", "IntrCity", "Delhi", "Jaipur", LocalTime.of(7, 30), LocalTime.of(13, 0), 45, 34, 650.0, "AC Semi-Sleeper", "Mineral Water, On-board Washroom, Wi-Fi", "https://images.unsplash.com/photo-1570125909232-eb263c188f7e?w=800"),
                createBus("SRS Scania Multi-Axle", "SRS-777", "SRS Travels", "Bengaluru", "Goa", LocalTime.of(21, 0), LocalTime.of(7, 30), 32, 18, 1400.0, "AC Sleeper", "Bedding, Reading Light, Emergency SOS", "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=800"),
                createBus("Orange BharatBenz AC Sleeper", "OR-505", "Orange Travels", "Mumbai", "Pune", LocalTime.of(10, 0), LocalTime.of(13, 30), 40, 30, 450.0, "AC Seater", "Express Expressway Route, High-Speed Wi-Fi", "https://images.unsplash.com/photo-1570125909232-eb263c188f7e?w=800"),
                createBus("VRL I-Shift Multi-Axle AC", "VRL-108", "VRL Travels", "Mumbai", "Goa", LocalTime.of(18, 30), LocalTime.of(6, 30), 38, 26, 1600.0, "AC Sleeper", "Snack Box, Sanitized Bedding, Water Bottle", "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=800")
        );

        busRepository.saveAll(buses);
        log.info("✅ Successfully seeded {} intercity buses.", buses.size());
    }

    private Bus createBus(String name, String number, String operator, String src, String dst, LocalTime dep, LocalTime arr, int total, int avail, double fare, String type, String amenities, String img) {
        Bus b = new Bus();
        b.setBusName(name);
        b.setBusNumber(number);
        b.setOperatorName(operator);
        b.setSource(src);
        b.setDestination(dst);
        b.setDepartureTime(dep);
        b.setArrivalTime(arr);
        b.setTotalSeats(total);
        b.setAvailableSeats(avail);
        b.setFare(fare);
        b.setBusType(type);
        b.setAmenities(amenities);
        b.setImageUrl(img);
        b.setStatus(true);
        return b;
    }
}
