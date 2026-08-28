import React, { useState } from "react";
import { loginHotel } from "../api/authApi";
import { useNavigate } from "react-router-dom";
import "../styles/Login.css";

function HotelLogin() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg("");
    setLoading(true);

    try {
      const res = await loginHotel({ email, password });

      if (res.error) {
        setErrorMsg(res.error.message || "Hotel login failed");
        return;
      }

      const hotelToken = res.data;
      if (!hotelToken) {
        setErrorMsg("Token not received from server");
        return;
      }

      // Clear conflicting role tokens
      localStorage.removeItem("token");
      localStorage.removeItem("adminToken");

      // Save hotel auth state
      localStorage.setItem("hotelToken", hotelToken);
      try {
        const payload = JSON.parse(atob(hotelToken.split(".")[1]));
        localStorage.setItem("email", payload.sub || email);
        localStorage.setItem("role", "HOTEL");
      } catch (e) {
        localStorage.setItem("email", email);
        localStorage.setItem("role", "HOTEL");
      }

      navigate("/hotel-login-dashboard");

    } catch (err) {
      console.error("Hotel login error:", err);
      const msg =
        err.response?.data?.error?.message ||
        err.response?.data?.message ||
        "Invalid hotel email or password";
      setErrorMsg(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <form onSubmit={handleSubmit}>
        <h2>Hotel Partner Login</h2>

        {errorMsg && <p style={{ color: "#f87171", fontSize: "14px", marginBottom: "12px" }}>{errorMsg}</p>}

        <input
          type="email"
          placeholder="Hotel Business Email"
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

        <button type="submit" disabled={loading}>
          {loading ? "Logging in..." : "Login as Hotelier"}
        </button>

        <p style={{ marginTop: "15px", fontSize: "15px", color: "#cbd5e1" }}>
          Don’t have a hotel partner account?{" "}
          <span
            style={{ color: "#38bdf8", cursor: "pointer", fontWeight: 600 }}
            onClick={() => navigate("/hotel-register")}
          >
            Register Hotel Here
          </span>
        </p>

        <div style={{ marginTop: "18px", paddingTop: "12px", borderTop: "1px solid rgba(255,255,255,0.1)", display: "flex", justifyContent: "space-between" }}>
          <span
            style={{ color: "#94a3b8", cursor: "pointer", fontSize: "13px" }}
            onClick={() => navigate("/login")}
          >
            👤 Traveler Login
          </span>
          <span
            style={{ color: "#94a3b8", cursor: "pointer", fontSize: "13px" }}
            onClick={() => navigate("/admin-login")}
          >
            🛡️ Admin Login
          </span>
        </div>
      </form>
    </div>
  );
}

export default HotelLogin;
