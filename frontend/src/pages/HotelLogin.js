import React, { useState } from "react";
import { loginHotel } from "../api/authApi";
import { useNavigate, Link } from "react-router-dom";
import { Eye, EyeOff, ShieldAlert, User, Shield } from "lucide-react";
import "../styles/Login.css";

function HotelLogin() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const navigate = useNavigate();

  const validateForm = () => {
    const errs = {};
    if (!email.trim()) {
      errs.email = "Hotel email is required";
    } else if (!/\S+@\S+\.\S+/.test(email)) {
      errs.email = "Please enter a valid email address";
    }
    if (!password) {
      errs.password = "Password is required";
    }
    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg("");

    if (!validateForm()) return;
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
        "Invalid hotel credentials. Please verify your email and password.";
      setErrorMsg(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page-wrapper">
      <div className="auth-card">
        {/* Brand Header */}
        <div className="auth-header">
          <div className="auth-brand-badge">
            <img src="/assets/logo-badge.png" alt="worldtours.com Logo" className="auth-logo-img" />
            <span style={{ fontWeight: 800, fontSize: "1.1rem" }}>worldtours.com</span>
          </div>
          <h2>Hotel Partner Portal</h2>
          <p>Access your hotel management dashboard</p>
        </div>

        {errorMsg && (
          <div className="auth-alert-error" style={{ marginBottom: "16px" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <ShieldAlert size={18} />
              <span>{errorMsg}</span>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form" noValidate>
          <div className="form-group-item">
            <label className="form-label" htmlFor="hotel-email">
              Hotel Business Email <span className="form-label-required">*</span>
            </label>
            <input
              id="hotel-email"
              type="email"
              className={`auth-input ${fieldErrors.email ? "input-error" : ""}`}
              placeholder="e.g. hotel@luxury.com"
              value={email}
              onChange={(e) => { setEmail(e.target.value); if (fieldErrors.email) setFieldErrors({ ...fieldErrors, email: "" }); }}
              disabled={loading}
              required
              autoComplete="email"
            />
            {fieldErrors.email && <span className="field-error-text">{fieldErrors.email}</span>}
          </div>

          <div className="form-group-item">
            <label className="form-label" htmlFor="hotel-password">
              Password <span className="form-label-required">*</span>
            </label>
            <div className="password-input-wrapper">
              <input
                id="hotel-password"
                type={showPassword ? "text" : "password"}
                className={`auth-input ${fieldErrors.password ? "input-error" : ""}`}
                placeholder="Enter hotel password"
                value={password}
                onChange={(e) => { setPassword(e.target.value); if (fieldErrors.password) setFieldErrors({ ...fieldErrors, password: "" }); }}
                disabled={loading}
                required
                autoComplete="current-password"
              />
              <button
                type="button"
                className="password-toggle-btn"
                onClick={() => setShowPassword(!showPassword)}
                aria-label={showPassword ? "Hide password" : "Show password"}
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
            {fieldErrors.password && <span className="field-error-text">{fieldErrors.password}</span>}
          </div>

          <button type="submit" className="auth-submit-btn" disabled={loading}>
            {loading ? "Authenticating Partner..." : "Sign In to Hotel Dashboard"}
          </button>
        </form>

        <div className="auth-footer-links">
          <div>
            New Hotel Partner?{" "}
            <Link to="/hotel-register" className="auth-link-highlight">
              Register Hotel here
            </Link>
          </div>

          <div className="auth-role-switch-row">
            <Link to="/login" className="auth-role-switch-btn">
              <User size={13} /> Traveler Login
            </Link>
            <Link to="/admin-login" className="auth-role-switch-btn">
              <Shield size={13} /> Admin Portal
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default HotelLogin;
