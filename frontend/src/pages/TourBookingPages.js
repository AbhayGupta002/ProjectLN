import React, { useState } from "react";
import { createBooking } from "../services/tourBookingService";

const TourBookingPage = () => {
  const [destination, setDestination] = useState("");
  const [hotelId, setHotelId] = useState("");
  const [checkIn, setCheckIn] = useState("");
  const [checkOut, setCheckOut] = useState("");
  const [guests, setGuests] = useState(1);
  const [message, setMessage] = useState("");

  const token = localStorage.getItem("token");
  const userId = localStorage.getItem("userId");

  const dummyHotels = [
    { id: 1, name: "Hotel Taj" },
    { id: 2, name: "Grand Palace Resort" },
    { id: 3, name: "Seaside View Hotel" }
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!destination || !hotelId || !checkIn || !checkOut) {
      setMessage("Please fill all fields.");
      return;
    }

    const bookingData = {
      userId,
      hotelId,
      destination,
      checkInDate: checkIn,
      checkOutDate: checkOut,
      totalGuests: guests
    };

    try {
      await createBooking(bookingData, token);
      setMessage("🎉 Booking Created Successfully!");
    } catch (error) {
      console.error(error);
      setMessage("❌ Something went wrong!");
    }
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>🧭 Smart AI Trip Planner</h2>

      <form style={styles.form} onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Enter Destination"
          value={destination}
          onChange={(e) => setDestination(e.target.value)}
          style={styles.input}
        />

        <select
          value={hotelId}
          onChange={(e) => setHotelId(e.target.value)}
          style={styles.input}
        >
          <option value="">Select Hotel</option>
          {dummyHotels.map((hotel) => (
            <option key={hotel.id} value={hotel.id}>
              {hotel.name}
            </option>
          ))}
        </select>

        <label style={styles.label}>Check-in Date</label>
        <input
          type="date"
          value={checkIn}
          onChange={(e) => setCheckIn(e.target.value)}
          style={styles.input}
        />

        <label style={styles.label}>Check-out Date</label>
        <input
          type="date"
          value={checkOut}
          onChange={(e) => setCheckOut(e.target.value)}
          style={styles.input}
        />

        <label style={styles.label}>Guests</label>
        <input
          type="number"
          min="1"
          value={guests}
          onChange={(e) => setGuests(e.target.value)}
          style={styles.input}
        />

        <button type="submit" style={styles.button}>
          Book Now
        </button>
      </form>

      {message && <p style={styles.message}>{message}</p>}
    </div>
  );
};

const styles = {
  container: {
    padding: "30px",
    width: "90%",
    maxWidth: "500px",
    margin: "auto",
    textAlign: "center"
  },
  title: {
    fontSize: "28px",
    fontWeight: "600",
    marginBottom: "20px"
  },
  form: {
    display: "flex",
    flexDirection: "column",
    gap: "15px"
  },
  label: {
    textAlign: "left",
    fontWeight: "600",
    marginTop: "5px"
  },
  input: {
    padding: "12px",
    borderRadius: "8px",
    border: "1px solid #ccc",
    fontSize: "16px"
  },
  button: {
    marginTop: "15px",
    padding: "12px",
    backgroundColor: "#00A8E8",
    color: "white",
    border: "none",
    fontSize: "18px",
    borderRadius: "8px",
    cursor: "pointer"
  },
  message: {
    marginTop: "15px",
    fontSize: "16px"
  }
};

export default TourBookingPage;