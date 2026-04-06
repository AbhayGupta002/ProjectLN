import React, { useState } from "react";
import { registerHotel } from "../api/authApi";

function HotelRegister() {
  const [formData, setFormData] = useState({
    hotel: "",
    email: "",
    password: "",
    city: "",
    address: "",
    price: "",
    roomAvl: "",
    location: ""
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      const res = await registerHotel(formData);
      alert("Hotel Registered Successfully!");
      console.log("Success",res);
    } catch (err) {
     const backendMessage =
                   err.response?.data?.error?.message ||      // if backend sends: response.setError(error)
                   err.response?.data?.message ||             // if backend sends: message field
                   "❌ Registration failed";

                 alert(backendMessage);
    }
  };

  return (
    <div className="page-container">
      <form onSubmit={handleRegister} className="form-box">
        <h2>Hotel Register</h2>

        <input
          type="text"
          name="hotel"
          placeholder="Hotel Name"
          value={formData.hotel}
          onChange={handleChange}
          required
        />

        <input
          type="email"
          name="email"
          placeholder="Email"
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
          placeholder="Address"
          value={formData.address}
          onChange={handleChange}
          required
        />

        <input
          type="number"
          name="price"
          placeholder="Price per Night"
          value={formData.price}
          onChange={handleChange}
          required
        />

        <input
          type="number"
          name="roomAvl"
          placeholder="Rooms Available"
          value={formData.roomAvl}
          onChange={handleChange}
          required
        />
        <input
                  type="text"
                  name="location"
                  placeholder="location"
                  value={formData.location}
                  onChange={handleChange}
                  required
                />

        <button type="submit">Register Hotel</button>

      </form>
    </div>
  );
}

export default HotelRegister;
