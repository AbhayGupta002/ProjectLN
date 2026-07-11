import React, { useState } from "react";
import { loginHotel } from "../api/authApi";
import { useNavigate } from "react-router-dom";
import "../styles/Login.css";

function HotelLogin() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const res = await loginHotel({ email, password });

      // -------------------------------
      // BACKEND STRUCTURE:
      // {
      //   "data": "JWT_TOKEN",
      //   "error": null
      // }
      // -------------------------------

      if (res.error) {
        alert(res.error.message || "Login failed");
        return;
      }

      const hotelToken = res.data;  // ✔ Correct: backend returns token inside data

      if (!hotelToken) {
        alert("Token not received from server");
        return;
      }

      // Save token
      localStorage.setItem("hotelToken", hotelToken);
      try {
        const payload = JSON.parse(atob(hotelToken.split(".")[1]));
        localStorage.setItem("email", payload.sub);
        localStorage.setItem("role", payload.role);
      } catch (e) {
        console.error("JWT Decode failed", e);
      }

      alert("✅ Login Successful!");
      navigate("/hotel-login-dashboard");

    } catch (err) {
      console.error(err);
      alert(err.response?.data?.error?.message || "Invalid credentials");
    }
  };

  return (
    <div className="page-container">
      <form onSubmit={handleSubmit}>
      <h2>Hotel Login</h2>
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        <button type="submit">Login</button>
        <p style={{ marginTop: "10px", fontSize: "18px", color: "white" }}>
                  Don’t have an account?{" "}
                  <span
                    style={{ color: "#00aaff", cursor: "pointer" }}
                    onClick={() => navigate("/hotel-register")}
                  >
                    Hotel Register Here
                  </span>
                </p>

      </form>


      </div>
  );
}

export default HotelLogin;
