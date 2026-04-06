// src/components/HotelCreateTour.jsx
import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import "../styles/HotelCreateTour.css";

function HotelCreateTour() {
  const [tourForm, setTourForm] = useState({
    title: "",
    description: "",
    price: "",
    durationDays: "",
    location: "",
    imageUrl: "",
  });

  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [hotelProfile, setHotelProfile] = useState({});
  const navigate = useNavigate();

  const token = localStorage.getItem("hotelToken") || localStorage.getItem("token");

  // Redirect to login if token missing
  useEffect(() => {
    if (!token) {
      navigate("/hotel-login");
    }
  }, [token, navigate]);

  // Fetch hotel profile
  useEffect(() => {
    if (!token) return;

    const fetchProfile = async () => {
      try {
        const res = await axios.get(
          "http://localhost:8080/api/hotellogindashboard/hotelprofile",
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        );
        setHotelProfile(res.data?.data || {});
      } catch (err) {
        console.error("Profile fetch failed", err);
        localStorage.removeItem("hotelToken");
        localStorage.removeItem("token");
        navigate("/hotel-login");
      }
    };

    fetchProfile();
  }, [token, navigate]);

  const handleChange = (e) => {
    setTourForm({ ...tourForm, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const payload = {
        ...tourForm,
        email: hotelProfile.email,
        hotelId: hotelProfile.id,
      };

      await axios.post(
        "http://localhost:8080/api/tour/create",
        payload,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setMessage("Tour created successfully!");
      setTourForm({
        title: "",
        description: "",
        price: "",
        durationDays: "",
        location: "",
        imageUrl: "",
      });
    } catch (err) {
      console.error(err);
      setMessage("Failed to create tour. Check console for details.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="hotel-tour-container">
      <h2>Create Tour Package</h2>
      {message && <p className="message">{message}</p>}
      <form onSubmit={handleSubmit} className="tour-form">
        <div className="form-group">
          <label>Title</label>
          <input
            type="text"
            name="title"
            value={tourForm.title}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label>Description</label>
          <textarea
            name="description"
            value={tourForm.description}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label>Price (₹)</label>
          <input
            type="number"
            name="price"
            value={tourForm.price}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label>Duration (days)</label>
          <input
            type="number"
            name="durationDays"
            value={tourForm.durationDays}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label>Location</label>
          <input
            type="text"
            name="location"
            value={tourForm.location}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label>Image URL</label>
          <input
            type="text"
            name="imageUrl"
            value={tourForm.imageUrl}
            onChange={handleChange}
          />
        </div>

        <button type="submit" disabled={loading}>
          {loading ? "Creating..." : "Create Tour"}
        </button>
      </form>
    </div>
  );
}

export default HotelCreateTour;