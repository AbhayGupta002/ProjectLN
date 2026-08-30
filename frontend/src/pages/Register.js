import React, { useState } from "react";
import { registerUser } from "../api/authApi";
import { useNavigate, Link } from "react-router-dom";
import { Eye, EyeOff, AlertCircle, Building2 } from "lucide-react";
import "../styles/Login.css";

function Register() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [mobile, setMobile] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const navigate = useNavigate();

  const validateForm = () => {
    const errs = {};

    if (!name.trim()) {
      errs.name = "Full Name is required";
    }

    if (!email.trim()) {
      errs.email = "Email address is required";
    } else if (!email.toLowerCase().endsWith("@gmail.com")) {
      errs.email = "Only official Google Mail (@gmail.com) is supported";
    }

    if (!mobile.trim()) {
      errs.mobile = "Mobile number is required";
    } else if (!/^\d{10}$/.test(mobile.trim())) {
      errs.mobile = "Please enter a valid 10-digit mobile number";
    }

    if (!password) {
      errs.password = "Password is required";
    } else if (password.length < 6) {
      errs.password = "Password must be at least 6 characters long";
    }

    if (password !== confirmPassword) {
      errs.confirmPassword = "Passwords do not match";
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
      const res = await registerUser({ name: name.trim(), email: email.trim(), mobile: mobile.trim(), password });

      if (res && res.error) {
        const errorText = typeof res.error === "string" ? res.error : (res.error.message || "Registration failed");
        setErrorMsg(errorText);
        return;
      }

      alert("✅ Registration successful! Please log in with your credentials.");
      navigate("/login");
    } catch (err) {
      console.error("Registration error:", err);
      const backendMessage =
        err.response?.data?.error?.message ||
        err.response?.data?.message ||
        (typeof err.response?.data?.error === "string" ? err.response.data.error : null) ||
        err.message ||
        "Registration failed. Please check your details.";
      setErrorMsg(backendMessage);
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
          <h2>Create Account</h2>
          <p>Join thousands of travelers enjoying luxury stays</p>
        </div>

        {errorMsg && (
          <div className="auth-alert-error" style={{ marginBottom: "16px" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <AlertCircle size={18} />
              <span>{errorMsg}</span>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form" noValidate>
          <div className="form-group-item">
            <label className="form-label" htmlFor="reg-name">
              Full Name <span className="form-label-required">*</span>
            </label>
            <input
              id="reg-name"
              type="text"
              className={`auth-input ${fieldErrors.name ? "input-error" : ""}`}
              placeholder="e.g. Rahul Sharma"
              value={name}
              onChange={(e) => { setName(e.target.value); if (fieldErrors.name) setFieldErrors({ ...fieldErrors, name: "" }); }}
              disabled={loading}
              required
            />
            {fieldErrors.name && <span className="field-error-text">{fieldErrors.name}</span>}
          </div>

          <div className="form-group-item">
            <label className="form-label" htmlFor="reg-email">
              Gmail Address <span className="form-label-required">*</span>
            </label>
            <input
              id="reg-email"
              type="email"
              className={`auth-input ${fieldErrors.email ? "input-error" : ""}`}
              placeholder="yourname@gmail.com"
              value={email}
              onChange={(e) => { setEmail(e.target.value); if (fieldErrors.email) setFieldErrors({ ...fieldErrors, email: "" }); }}
              disabled={loading}
              required
            />
            {fieldErrors.email ? (
              <span className="field-error-text">{fieldErrors.email}</span>
            ) : (
              <span className="field-helper-text">Must end with @gmail.com for verification</span>
            )}
          </div>

          <div className="form-group-item">
            <label className="form-label" htmlFor="reg-mobile">
              Mobile Number (10 digits) <span className="form-label-required">*</span>
            </label>
            <input
              id="reg-mobile"
              type="tel"
              maxLength={10}
              className={`auth-input ${fieldErrors.mobile ? "input-error" : ""}`}
              placeholder="9876543210"
              value={mobile}
              onChange={(e) => { setMobile(e.target.value); if (fieldErrors.mobile) setFieldErrors({ ...fieldErrors, mobile: "" }); }}
              disabled={loading}
              required
            />
            {fieldErrors.mobile && <span className="field-error-text">{fieldErrors.mobile}</span>}
          </div>

          <div className="form-group-item">
            <label className="form-label" htmlFor="reg-password">
              Create Password <span className="form-label-required">*</span>
            </label>
            <div className="password-input-wrapper">
              <input
                id="reg-password"
                type={showPassword ? "text" : "password"}
                className={`auth-input ${fieldErrors.password ? "input-error" : ""}`}
                placeholder="At least 6 characters"
                value={password}
                onChange={(e) => { setPassword(e.target.value); if (fieldErrors.password) setFieldErrors({ ...fieldErrors, password: "" }); }}
                disabled={loading}
                required
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

          <div className="form-group-item">
            <label className="form-label" htmlFor="reg-confirm-password">
              Confirm Password <span className="form-label-required">*</span>
            </label>
            <div className="password-input-wrapper">
              <input
                id="reg-confirm-password"
                type={showConfirmPassword ? "text" : "password"}
                className={`auth-input ${fieldErrors.confirmPassword ? "input-error" : ""}`}
                placeholder="Re-enter password"
                value={confirmPassword}
                onChange={(e) => { setConfirmPassword(e.target.value); if (fieldErrors.confirmPassword) setFieldErrors({ ...fieldErrors, confirmPassword: "" }); }}
                disabled={loading}
                required
              />
              <button
                type="button"
                className="password-toggle-btn"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                aria-label={showConfirmPassword ? "Hide confirm password" : "Show confirm password"}
              >
                {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
            {fieldErrors.confirmPassword && <span className="field-error-text">{fieldErrors.confirmPassword}</span>}
          </div>

          <button type="submit" className="auth-submit-btn" disabled={loading}>
            {loading ? "Creating Account..." : "Create Traveler Account"}
          </button>
        </form>

        <div className="auth-footer-links">
          <div>
            Already have an account?{" "}
            <Link to="/login" className="auth-link-highlight">
              Log In here
            </Link>
          </div>

          <div className="auth-role-switch-row" style={{ justifyContent: "center" }}>
            <Link to="/hotel-register" className="auth-role-switch-btn">
              <Building2 size={13} /> Partner with us? Register Hotel
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Register;
