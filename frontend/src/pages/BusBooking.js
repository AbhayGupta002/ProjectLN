import React, { useState, useEffect } from "react";
import { searchBuses, bookBus, getAllBuses } from "../api/busBookingApi";
import { startPayment } from "../payment/RazorpayPayment";
import HomeNavbar from "../components/HomeNavbar";
import { Info, Search, MapPin, Calendar, Users, X } from "lucide-react";
import "../styles/BusBooking.css";

function BusBooking() {
  const [source, setSource] = useState("");
  const [destination, setDestination] = useState("");
  const [buses, setBuses] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedBus, setSelectedBus] = useState(null);
  const [showModal, setShowModal] = useState(false);

  // Booking details form
  const [passengerName, setPassengerName] = useState("");
  const [passengerAge, setPassengerAge] = useState("");
  const [passengerGender, setPassengerGender] = useState("Male");
  const [numberOfSeats, setNumberOfSeats] = useState(1);
  const [journeyDate, setJourneyDate] = useState("");

  // Load all buses initially
  useEffect(() => {
    fetchInitialBuses();
  }, []);

  const fetchInitialBuses = async () => {
    try {
      setLoading(true);
      const res = await getAllBuses();
      if (res.data && res.data.data) {
        setBuses(res.data.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!source || !destination) {
      alert("Please fill in source and destination");
      return;
    }
    try {
      setLoading(true);
      const res = await searchBuses(source, destination);
      if (res.data && res.data.data) {
        setBuses(res.data.data);
      } else {
        setBuses([]);
      }
    } catch (err) {
      console.error(err);
      setBuses([]);
    } finally {
      setLoading(false);
    }
  };

  const openBookingModal = (bus) => {
    if (!localStorage.getItem("token")) {
      alert("Please login first to book bus tickets!");
      window.location.href = "/login";
      return;
    }
    setSelectedBus(bus);
    setShowModal(true);
  };

  const handleBookingPayment = async (e) => {
    e.preventDefault();
    if (!passengerName || !passengerAge || !journeyDate) {
      alert("Please fill in all details");
      return;
    }

    const totalAmount = selectedBus.fare * numberOfSeats;

    // Trigger Razorpay payment gateway
    await startPayment(
      totalAmount,
      async (paymentRes) => {
        // Payment Success Callback -> Submit Booking
        try {
          const userId = localStorage.getItem("userId") || 1;
          const bookingData = {
            userId: Number(userId),
            busId: selectedBus.id,
            passengerName,
            passengerAge: Number(passengerAge),
            passengerGender,
            numberOfSeats: Number(numberOfSeats),
            journeyDate,
          };

          const bookRes = await bookBus(bookingData);
          if (bookRes.data && bookRes.data.success) {
            alert(`Bus ticket booked successfully! Booking Reference ID: ${bookRes.data.data.id}`);
            setShowModal(false);
            fetchInitialBuses(); // refresh seat availability
          } else {
            alert(bookRes.data.message || "Booking failed.");
          }
        } catch (err) {
          console.error(err);
          alert("Booking registration failed, contact support.");
        }
      },
      (err) => {
        // Payment Failure Callback
        console.error(err);
        alert("Payment was not completed. Booking cancelled.");
      }
    );
  };

  return (
    <div style={{ backgroundColor: "#f8fafc", minHeight: "100vh" }}>
      <HomeNavbar />
      <div className="booking-container">
        <div className="booking-header">
          <h1>Book Bus Tickets</h1>
          <p>Compare and book bus routes with premium operators and comfortable amenities</p>
        </div>

        {/* Search Bar Component */}
        <form className="search-card" onSubmit={handleSearch}>
          <div className="search-grid">
            <div className="input-group">
              <label><MapPin size={16} style={{ marginRight: 4, verticalAlign: "middle" }} /> From</label>
              <input
                type="text"
                placeholder="Origin City (e.g. Pune)"
                value={source}
                onChange={(e) => setSource(e.target.value)}
                required
              />
            </div>
            <div className="input-group">
              <label><MapPin size={16} style={{ marginRight: 4, verticalAlign: "middle" }} /> To</label>
              <input
                type="text"
                placeholder="Destination City (e.g. Bangalore)"
                value={destination}
                onChange={(e) => setDestination(e.target.value)}
                required
              />
            </div>
          </div>
          <div style={{ display: "flex", justifyContent: "flex-end" }}>
            <button className="search-btn" type="submit">
              <Search size={18} /> Search Buses
            </button>
          </div>
        </form>

        {/* Bus Results */}
        <div className="results-section">
          <h2>Available Buses</h2>
          {loading ? (
            <div className="no-results">Searching best buses for you...</div>
          ) : buses.length === 0 ? (
            <div className="no-results">No buses found for this route. Try another search.</div>
          ) : (
            <div className="results-grid">
              {buses.map((bus) => (
                <div className="result-card" key={bus.id}>
                  <div className="bus-info">
                    <div className="bus-badge">
                      {bus.operatorName}
                    </div>
                    <div className="route-details">
                      <div className="time-place">
                        <h3>{bus.departureTime ? bus.departureTime.substring(0,5) : "08:00"}</h3>
                        <p>{bus.source}</p>
                      </div>
                      <div className="route-line">
                        <span>{bus.busNumber}</span>
                        <div className="line-dots"></div>
                        <span style={{ marginTop: 4 }}>{bus.busType || "Sleeper AC"}</span>
                      </div>
                      <div className="time-place">
                        <h3>{bus.arrivalTime ? bus.arrivalTime.substring(0,5) : "18:00"}</h3>
                        <p>{bus.destination}</p>
                      </div>
                    </div>
                  </div>
                  <div className="price-book-group">
                    <div className="fare-info">
                      <p>Per Seat</p>
                      <h2>₹{bus.fare}</h2>
                      <span style={{ fontSize: "0.85rem", color: bus.availableSeats < 10 ? "#ef4444" : "#10b981", fontWeight: 600 }}>
                        {bus.availableSeats} seats left
                      </span>
                    </div>
                    <button className="book-now-btn" onClick={() => openBookingModal(bus)}>
                      Book Seats
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Booking Form Modal */}
      {showModal && selectedBus && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2>Confirm Passenger Details</h2>
              <button className="close-btn" onClick={() => setShowModal(false)}><X size={24} /></button>
            </div>
            <div style={{ background: "#f8fafc", padding: 15, borderRadius: 12, marginBottom: 20, fontSize: "0.9rem" }}>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
                <strong>Operator:</strong> <span>{selectedBus.operatorName} ({selectedBus.busNumber})</span>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
                <strong>Route:</strong> <span>{selectedBus.source} → {selectedBus.destination}</span>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <strong>Price per Seat:</strong> <span style={{ color: "#10b981", fontWeight: 700 }}>₹{selectedBus.fare}</span>
              </div>
            </div>

            <form className="modal-form" onSubmit={handleBookingPayment}>
              <div className="input-group">
                <label><Users size={16} style={{ marginRight: 4, verticalAlign: "middle" }} /> Passenger Name</label>
                <input
                  type="text"
                  placeholder="Enter Full Name"
                  value={passengerName}
                  onChange={(e) => setPassengerName(e.target.value)}
                  required
                />
              </div>
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 15 }}>
                <div className="input-group">
                  <label>Age</label>
                  <input
                    type="number"
                    placeholder="Age"
                    value={passengerAge}
                    onChange={(e) => setPassengerAge(e.target.value)}
                    required
                  />
                </div>
                <div className="input-group">
                  <label>Gender</label>
                  <select value={passengerGender} onChange={(e) => setPassengerGender(e.target.value)}>
                    <option value="Male">Male</option>
                    <option value="Female">Female</option>
                    <option value="Other">Other</option>
                  </select>
                </div>
              </div>
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 15 }}>
                <div className="input-group">
                  <label><Calendar size={16} style={{ marginRight: 4, verticalAlign: "middle" }} /> Journey Date</label>
                  <input
                    type="date"
                    value={journeyDate}
                    onChange={(e) => setJourneyDate(e.target.value)}
                    required
                  />
                </div>
                <div className="input-group">
                  <label>Number of Seats</label>
                  <input
                    type="number"
                    min="1"
                    max={selectedBus.availableSeats}
                    value={numberOfSeats}
                    onChange={(e) => setNumberOfSeats(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div style={{ borderTop: "1px solid #e2e8f0", padding: "15px 0 0 0", marginTop: 10, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                  <span style={{ color: "#64748b", fontSize: "0.9rem" }}>Total Price</span>
                  <h3 style={{ margin: 0, fontSize: "1.5rem", fontWeight: 800, color: "#0f172a" }}>
                    ₹{selectedBus.fare * numberOfSeats}
                  </h3>
                </div>
                <div className="modal-actions" style={{ margin: 0 }}>
                  <button type="submit" className="pay-btn" style={{ background: "linear-gradient(135deg, #f97316, #ea580c)" }}>Proceed to Pay</button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default BusBooking;
