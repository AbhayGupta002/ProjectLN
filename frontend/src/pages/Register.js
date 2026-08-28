import React, { useState } from "react";
import { registerUser } from "../api/authApi";
import { useNavigate } from "react-router-dom";
import "../styles/Login.css";

function Register() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [mobile, setMobile] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg("");

    if (!email.endsWith("@gmail.com")) {
      setErrorMsg("Only Gmail addresses (@gmail.com) are allowed!");
      return;
    }

    setLoading(true);
    try {
      await registerUser({ name, email, mobile, password });
      alert("✅ Registration successful! Please log in with your credentials.");
      navigate("/login");
    } catch (err) {
      console.error("Registration error:", err.response?.data);
      const backendMessage =
        err.response?.data?.error?.message ||
        err.response?.data?.message ||
        "Registration failed. Please check your details.";
      setErrorMsg(backendMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <form onSubmit={handleSubmit}>
        <h2>User Register</h2>

        {errorMsg && <p style={{ color: "#f87171", fontSize: "14px", marginBottom: "12px" }}>{errorMsg}</p>}

        <input
          type="text"
          placeholder="Full Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />

        <input
          type="email"
          placeholder="Email Address (must end with @gmail.com)"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        <input
          type="tel"
          placeholder="Mobile Number (10 digits)"
          value={mobile}
          onChange={(e) => setMobile(e.target.value)}
          minLength={10}
          maxLength={13}
          required
        />

        <input
          type="password"
          placeholder="Password (min 5 characters)"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          minLength={5}
          maxLength={20}
          required
        />

        <button type="submit" disabled={loading}>
          {loading ? "Registering..." : "Create Account"}
        </button>

        <p style={{ marginTop: "15px", fontSize: "15px", color: "#cbd5e1" }}>
          Already have an account?{" "}
          <span
            style={{ color: "#38bdf8", cursor: "pointer", fontWeight: 600 }}
            onClick={() => navigate("/login")}
          >
            Log in here
          </span>
        </p>

        <div style={{ marginTop: "18px", paddingTop: "12px", borderTop: "1px solid rgba(255,255,255,0.1)" }}>
          <span
            style={{ color: "#94a3b8", cursor: "pointer", fontSize: "13px" }}
            onClick={() => navigate("/hotel-register")}
          >
            🏨 Are you a hotel owner? Register your hotel here
          </span>
        </div>
      </form>
    </div>
  );
}

export default Register;
