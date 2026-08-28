import React, { useState } from "react";
import { registerHotel } from "../api/authApi";
import { useNavigate } from "react-router-dom";
import "../styles/Login.css";

function HotelRegister() {
  const [formData, setFormData] = useState({
    hotel: "",
    email: "",
    password: "",
    city: "",
    address: "",
    price: "",
    roomAvailable: "",
    location: ""
  });
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setErrorMsg("");

    if (!formData.email.endsWith("@gmail.com")) {
      setErrorMsg("Only Gmail addresses (@gmail.com) are allowed!");
      return;
    }

    setLoading(true);
    try {
      await registerHotel(formData);
      alert("✅ Hotel registered successfully! Please login with your hotel credentials.");
      navigate("/hotel-login");
    } catch (err) {
      console.error("Hotel registration error:", err.response?.data);
      const backendMessage =
        err.response?.data?.error?.message ||
        err.response?.data?.message ||
        "Registration failed. Please verify your details.";
      setErrorMsg(backendMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <form onSubmit={handleRegister} className="form-box">
        <h2>Register Hotel Partner</h2>

        {errorMsg && <p style={{ color: "#f87171", fontSize: "14px", marginBottom: "12px" }}>{errorMsg}</p>}

        <input
          type="text"
          name="hotel"
          placeholder="Hotel Property Name"
          value={formData.hotel}
          onChange={handleChange}
          required
        />

        <input
          type="email"
          name="email"
          placeholder="Business Email (must end with @gmail.com)"
          value={formData.email}
          onChange={handleChange}
          required
        />

        <input
          type="password"
          name="password"
          placeholder="Password"
          value={formData.password}
          onChange={handleChange}
          minLength={5}
          required
        />

        <input
          type="text"
          name="city"
          placeholder="City"
          value={formData.city}
          onChange={handleChange}
          required
        />

        <input
          type="text"
          name="address"
          placeholder="Full Address"
          value={formData.address}
          onChange={handleChange}
          required
        />

        <input
          type="number"
          name="price"
          placeholder="Price per Night (₹)"
          value={formData.price}
          onChange={handleChange}
          required
        />

        <input
          type="number"
          name="roomAvailable"
          placeholder="Rooms Available"
          value={formData.roomAvailable}
          onChange={handleChange}
          required
        />

        <input
          type="text"
          name="location"
          placeholder="Landmark / Location"
          value={formData.location}
          onChange={handleChange}
          required
        />

        <button type="submit" disabled={loading}>
          {loading ? "Registering Property..." : "Register Hotel"}
        </button>

        <p style={{ marginTop: "15px", fontSize: "15px", color: "#cbd5e1" }}>
          Already registered as a partner?{" "}
          <span
            style={{ color: "#38bdf8", cursor: "pointer", fontWeight: 600 }}
            onClick={() => navigate("/hotel-login")}
          >
            Hotel Login
          </span>
        </p>

        <div style={{ marginTop: "18px", paddingTop: "12px", borderTop: "1px solid rgba(255,255,255,0.1)" }}>
          <span
            style={{ color: "#94a3b8", cursor: "pointer", fontSize: "13px" }}
            onClick={() => navigate("/login")}
          >
            👤 Looking for traveler login? Click here
          </span>
        </div>
      </form>
    </div>
  );
}

export default HotelRegister;
