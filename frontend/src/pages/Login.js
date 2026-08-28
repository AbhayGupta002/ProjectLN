import React, { useState } from "react";
import { loginUser, verify2fa } from "../api/authApi";
import { useNavigate, Link } from "react-router-dom";
import { Eye, EyeOff, ShieldAlert, Building2, Shield } from "lucide-react";
import "../styles/Login.css";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [otp, setOtp] = useState("");
  const [step2fa, setStep2fa] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");
  const [statusMsg, setStatusMsg] = useState("");
  const [isLocked, setIsLocked] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});
  const navigate = useNavigate();

  const handleAuthSuccess = (token) => {
    // Clear conflicting role tokens
    localStorage.removeItem("hotelToken");
    localStorage.removeItem("adminToken");

    localStorage.setItem("token", token);
    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      localStorage.setItem("email", payload.sub || email);
      localStorage.setItem("role", "USER");
    } catch (e) {
      localStorage.setItem("email", email);
      localStorage.setItem("role", "USER");
    }

    navigate("/dashboard");
  };

  const validateForm = () => {
    const errs = {};
    if (!email.trim()) {
      errs.email = "Email address is required";
    } else if (!/\S+@\S+\.\S+/.test(email)) {
      errs.email = "Please enter a valid email address";
    }
    if (!password) {
      errs.password = "Password is required";
    }
    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleCredentialsSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg("");
    setStatusMsg("");
    setIsLocked(false);

    if (!validateForm()) return;
    setLoading(true);

    try {
      const res = await loginUser({ email, password });

      if (res.error) {
        setErrorMsg(res.error.message || "Login failed");
        return;
      }

      // Check if 2FA OTP verification is required
      if (res.data && typeof res.data === "object" && res.data.requires2fa) {
        setStep2fa(true);
        if (res.data.devOtp) {
          setOtp(res.data.devOtp);
        }
        setStatusMsg(res.data.message || "2FA OTP sent to your email. Please enter the OTP below.");
        return;
      }

      // Direct token returned
      const token = typeof res.data === "string" ? res.data : (res.data?.token || res.data);
      if (token) {
        handleAuthSuccess(token);
      } else {
        setErrorMsg("Unexpected server response format");
      }

    } catch (err) {
      console.error("Login error:", err);
      const status = err.response?.status;
      const backendError = err.response?.data?.error?.message || err.response?.data?.message;

      if (status === 423 || (backendError && backendError.toLowerCase().includes("locked"))) {
        setIsLocked(true);
        setErrorMsg(backendError || "Your account has been temporarily locked due to 4 consecutive failed login attempts.");
      } else if (status === 401) {
        setErrorMsg(backendError || "Invalid email or password. Please check your credentials.");
      } else {
        setErrorMsg(backendError || "Login failed. Please check your internet connection or try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleOtpSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg("");
    setStatusMsg("");

    if (!otp.trim()) {
      setErrorMsg("Please enter the verification code");
      return;
    }

    setLoading(true);
    try {
      const res = await verify2fa({ email, otp: otp.trim() });

      if (res.error) {
        setErrorMsg(res.error.message || "2FA verification failed");
        return;
      }

      const token = typeof res.data === "string" ? res.data : (res.data?.token || res.data);
      if (token) {
        handleAuthSuccess(token);
      } else {
        setErrorMsg("Token not received after 2FA verification");
      }
    } catch (err) {
      console.error("2FA Error:", err);
      setErrorMsg(err.response?.data?.error?.message || err.response?.data?.message || "Invalid or expired OTP code.");
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
          <h2>{!step2fa ? "Traveler Login" : "Two-Factor Verification"}</h2>
          <p>{!step2fa ? "Sign in to manage your stays and trips" : "Enter the verification code sent to your email"}</p>
        </div>

        {/* Status / Error Alerts */}
        {errorMsg && (
          <div className="auth-alert-error" style={{ marginBottom: "16px" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <ShieldAlert size={18} />
              <span>{errorMsg}</span>
            </div>
            {isLocked && (
              <div style={{ marginTop: "6px" }}>
                <Link to="/forgot-password" style={{ color: "#38bdf8", fontWeight: 700 }}>
                  Unlock via Forgot Password ➔
                </Link>
              </div>
            )}
          </div>
        )}

        {statusMsg && (
          <div className="auth-alert-info" style={{ marginBottom: "16px" }}>
            {statusMsg}
          </div>
        )}

        {!step2fa ? (
          /* Step 1: Email & Password */
          <form onSubmit={handleCredentialsSubmit} className="auth-form" noValidate>
            <div className="form-group-item">
              <label className="form-label" htmlFor="user-email">
                Email Address <span className="form-label-required">*</span>
              </label>
              <input
                id="user-email"
                type="email"
                className={`auth-input ${fieldErrors.email ? "input-error" : ""}`}
                placeholder="e.g. traveler@gmail.com"
                value={email}
                onChange={(e) => { setEmail(e.target.value); if (fieldErrors.email) setFieldErrors({ ...fieldErrors, email: "" }); }}
                disabled={loading}
                required
                autoComplete="email"
              />
              {fieldErrors.email && <span className="field-error-text">{fieldErrors.email}</span>}
            </div>

            <div className="form-group-item">
              <label className="form-label" htmlFor="user-password">
                Password <span className="form-label-required">*</span>
              </label>
              <div className="password-input-wrapper">
                <input
                  id="user-password"
                  type={showPassword ? "text" : "password"}
                  className={`auth-input ${fieldErrors.password ? "input-error" : ""}`}
                  placeholder="Enter your password"
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

            <Link to="/forgot-password" className="auth-forgot-link">
              Forgot your password?
            </Link>

            <button type="submit" className="auth-submit-btn" disabled={loading}>
              {loading ? "Authenticating..." : "Sign In to Account"}
            </button>
          </form>
        ) : (
          /* Step 2: 2FA OTP */
          <form onSubmit={handleOtpSubmit} className="auth-form" noValidate>
            <div className="form-group-item">
              <label className="form-label" htmlFor="otp-input">
                6-Digit Security Code <span className="form-label-required">*</span>
              </label>
              <input
                id="otp-input"
                type="text"
                className="auth-input"
                placeholder="Enter 6-digit OTP"
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
                maxLength={6}
                disabled={loading}
                autoFocus
                required
              />
              <span className="field-helper-text">Check your inbox for the one-time passcode.</span>
            </div>

            <button type="submit" className="auth-submit-btn" disabled={loading}>
              {loading ? "Verifying OTP..." : "Verify & Complete Login"}
            </button>

            <button
              type="button"
              className="auth-role-switch-btn"
              style={{ justifyContent: "center", marginTop: "10px" }}
              onClick={() => { setStep2fa(false); setErrorMsg(""); setStatusMsg(""); }}
            >
              ← Back to password login
            </button>
          </form>
        )}

        {/* Footer Navigation */}
        <div className="auth-footer-links">
          <div>
            Don't have an account?{" "}
            <Link to="/register" className="auth-link-highlight">
              Sign Up here
            </Link>
          </div>

          <div className="auth-role-switch-row">
            <Link to="/hotel-login" className="auth-role-switch-btn">
              <Building2 size={13} /> Hotel Partner
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

export default Login;
