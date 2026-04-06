import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { searchTours } from "../api/publicApi";
import axios from "axios";
import { searchByLocation,searchTourByDays } from "../api/publicApi";
import "../styles/HomePage.css";

function HomePage() {

  const navigate = useNavigate();
  const today = new Date().toISOString().split("T")[0];
  const [checkIn, setCheckIn] = useState("");
  const [checkOut, setCheckOut] = useState("");
  const [guests, setGuests] = useState("");
  const[days, setDays] = useState("");
  const [location, setLocation] = useState("");
  const [searchLocation, setSearchLocation] = useState("");
  const [loading, setLoading] = useState(false);
  const [tours, setTours] = useState([]);
  const [showResults, setShowResults] = useState(false);
  const [hotels, setHotels] = useState([]);


useEffect(() => {
  fetchTopHotels();
}, []);

const fetchTopHotels = async () => {
  try {
    const res = await axios.get("http://localhost:8080/api/public/tophotels");

    console.log("FULL RESPONSE:", res.data);

    const hotelsData = res.data.body?.data || [];

    setHotels(hotelsData);

  } catch (err) {
    console.error(err);
  }
};

 // 🔥 Calculate days
  const calculateDays = () => {
    if (!checkIn || !checkOut) return 0;

    const start = new Date(checkIn);
    const end = new Date(checkOut);

    const diffTime = end - start;
    const days = diffTime / (1000 * 60 * 60 * 24);

    return days > 0 ? days : 0;
  };

 // 🔥 HANDLE SEARCH
const handleSearch = async () => {
  if (!searchLocation && !days) {
    alert("Enter location OR days");
    return;
  }

  try {
    setLoading(true);
    let res;

    if (searchLocation) {
      res = await searchByLocation(searchLocation);
    } else {
      res = await searchTourByDays(days);
    }

    if (res.error) {
      alert(res.error.message);
      setTours([]);
    } else {
      setTours(res.data);
      setShowResults(true); // 🔥 SHOW POPUP
    }

  } catch (err) {
    console.error(err);
    alert("Error fetching tours");
  } finally {
    setLoading(false);
  }
};



  return (
    <div className="home">


      {/* 🔥 NAVBAR */}
      <nav className="navbar">
        <h2>HotelBooking Tours & Travels</h2>
        <div>
          <span onClick={() => navigate("/")}>Home</span>
          <span onClick={() => navigate("/")}>Hotels</span>
          <span onClick={() => navigate("/")}>About</span>
          <span onClick={() => navigate("/")}>Contact</span>
        </div>
      </nav>
      {showResults && (
        <div className="results-overlay">

          <div className="results-box">

            {/* ❌ CLOSE BUTTON */}
            <button
              className="close-btn"
              onClick={() => setShowResults(false)}
            >
              ✖
            </button>

            <h2>Search Results</h2>

            <div className="card-container">

              {tours.length > 0 ? (
                tours.map((t, i) => (
                  <div key={i} className="card">
                    <img src={t.imageUrl} alt={t.title} />
                    <h3>{t.title}</h3>
                    <p>{t.location}</p>
                    <p>{t.durationDays} Days</p>
                    <p>₹{t.price}</p>
                  </div>
                ))
              ) : (
                <p>No tours found</p>
              )}

            </div>
          </div>
        </div>
      )}


      {/* 🔥 HERO SECTION */}
      <section className="hero">
        <h1>Find Your Perfect Stay</h1>
        <p>Luxury rooms at affordable prices</p>

        <div className="search-box">
           <input
                  type="date"
                  min={today}
                  value={checkIn}
                  onChange={(e) => setCheckIn(e.target.value)}
                />

                <input
                  type="date"
                  min={checkIn || today}
                  value={checkOut}
                  onChange={(e) => setCheckOut(e.target.value)}
                />
                <input
                            type="text"
                            placeholder="Enter location (Goa, Delhi...)"
                            value={searchLocation}
                            onChange={(e) => {
                              setSearchLocation(e.target.value);
                              setDays(""); // clear days
                            }}
                          />

               {/* 🔥 DAYS */}
                         <input
                           type="number"
                           placeholder="Enter days (e.g. 5)"
                           value={days}
                           onChange={(e) => {
                             setDays(e.target.value);
                             setSearchLocation(""); // clear location
                           }}
                           min="1"
                         />
                <button onClick={handleSearch}>
                            {loading ? "Searching..." : "Search"}
                          </button>
        </div>
      </section>

      {/* 🔥 FEATURED HOTELS */}
    <section className="featured-section">
      <h2 className="featured-title">Featured Hotels</h2>

      <div className="featured-container">
        {hotels.length > 0 ? (
          hotels.map((hotel, index) => (
            <div className="featured-card" key={hotel.id}>

              <img
                className="featured-img"
                src={`/images/hotel${(index % 3) + 1}.jpg`}
                alt="hotel"
              />

              <div className="featured-content">
                <h3 className="hotel-name">{hotel.hotel}</h3>

                <p className="hotel-city">{hotel.city}</p>

                <p className="hotel-price">₹{hotel.price} / night</p>

                <button className="book-btn">Book Now</button>
              </div>

            </div>
          ))
        ) : (
          <p className="no-data">No hotels found</p>
        )}
      </div>
    </section>

      {/* 🔥 QUICK ACCESS SECTION (YOUR OLD UI) */}
      <section className="section quick-access">
        <h2><u>Login & REGISTERED</u></h2>

        <div className="cards-wrapper">
          {[
            {
              title: "User Login",
              subtitle: "Sign in to your account",
              color: "#0A68FE",
              link: "/login",
            },
            {
              title: "User Registration",
              subtitle: "Create a new user account",
              color: "#0DBA45",
              link: "/register",
            },
            {
              title: "Hotel Login",
              subtitle: "Access hotel dashboard",
              color: "#9B27F0",
              link: "/hotel-login",
            },
            {
              title: "Hotel Registration",
              subtitle: "Register your hotel",
              color: "#FF6A00",
              link: "/hotel-register",
            },
            {
              title: "Admin Panel",
              subtitle: "Admin Login",
              color: "#00CED1",
              link: "/AdminLogin",
            },
            {
              title: "Trip Planner",
              subtitle: "Plan your trip",
              color: "#00A8E8",
              link: "/",
            }
          ].map((item, index) => (
            <div
              key={index}
              className="quick-card"
              onClick={() => navigate(item.link)}
            >
              <div
                className="icon"
                style={{
                  backgroundColor: item.color + "20",
                  color: item.color
                }}
              >
                ●
              </div>

              <h3>{item.title}</h3>
              <p>{item.subtitle}</p>

              <button style={{ backgroundColor: item.color }}>
                Get Started
              </button>
            </div>
          ))}
        </div>
      </section>

      {/* 🔥 WHY CHOOSE US */}
      <section className="section dark">
        <h2>Why Choose Us</h2>
        <div className="features">
          <div>✅ Best Price</div>
          <div>🏨 1000+ Hotels</div>
          <div>⭐ Top Rated</div>
          <div>🔒 Secure Booking</div>
        </div>
      </section>

      {/* 🔥 AMENITIES */}
      <section className="section">
        <h2>Amenities</h2>
        <div className="features">
          <div>📶 Free WiFi</div>
          <div>🍽️ Restaurant</div>
          <div>🏊 Pool</div>
          <div>🚗 Parking</div>
        </div>
      </section>

      {/* 🔥 OFFERS */}
      <section className="section offer">
        <h2>Special Offers</h2>
        <p>Get 20% OFF on weekend bookings</p>
        <button>Explore Deals</button>
      </section>

      {/* 🔥 REVIEWS */}
      <section className="section">
        <h2>What Our Customers Say</h2>
        <div className="card-container">
          <div className="card small">
            <p>"Amazing experience!" ⭐⭐⭐⭐⭐</p>
            <h4>- Rahul</h4>
          </div>
          <div className="card small">
            <p>"Best hotel ever!" ⭐⭐⭐⭐</p>
            <h4>- Priya</h4>
          </div>
        </div>
      </section>

      {/* 🔥 GALLERY */}
      <section className="section">
        <h2>Gallery</h2>
        <div className="gallery">
          {[1,2,3,4].map((i) => (
            <img key={i} src={`/images/hotel${i}.jpg`} alt="gallery"/>
          ))}
        </div>
      </section>

      {/* 🔥 CTA */}
      <section className="cta">
        <h2>Book Your Dream Stay Now</h2>
        <button onClick={() => navigate("/view-hotels")}>Book Now</button>
      </section>

      {/* 🔥 FOOTER */}
      <footer className="footer">
        <p>© 2026 HotelBooking | All Rights Reserved</p>
        <p>Email: support@hotel.com</p>
      </footer>

    </div>
  );
}

export default HomePage;