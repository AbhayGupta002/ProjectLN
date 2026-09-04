import React, { useState, useEffect } from "react";
import { registerUser, sendRegistrationOtp } from "../api/authApi";
import { useNavigate, Link } from "react-router-dom";
import { Eye, EyeOff, AlertCircle, CheckCircle2, ShieldCheck, ArrowLeft, RefreshCw, Building2 } from "lucide-react";
import "../styles/Login.css";

function Register() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [mobile, setMobile] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // OTP State
  const [step, setStep] = useState(1); // 1: Info Form, 2: OTP Verification
  const [otp, setOtp] = useState("");
  const [resendTimer, setResendTimer] = useState(60);
  const [canResend, setCanResend] = useState(false);

  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");
  const [successMsg, setSuccessMsg] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const navigate = useNavigate();

  // Resend timer countdown
  useEffect(() => {
    let timer;
    if (step === 2 && resendTimer > 0) {
      timer = setInterval(() => {
        setResendTimer((prev) => {
          if (prev <= 1) {
            setCanResend(true);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    }
    return () => clearInterval(timer);
  }, [step, resendTimer]);

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

  // Step 1: Send Registration OTP to Email
  const handleSendOtp = async (e) => {
    e.preventDefault();
    setErrorMsg("");
    setSuccessMsg("");

    if (!validateForm()) return;

    setLoading(true);
    try {
      const res = await sendRegistrationOtp({
        name: name.trim(),
        email: email.trim(),
        mobile: mobile.trim(),
        password,
      });

      if (res && res.error) {
        const errorText = typeof res.error === "string" ? res.error : (res.error.message || "Failed to send OTP");
        setErrorMsg(errorText);
        return;
      }

      setStep(2);
      setResendTimer(60);
      setCanResend(false);
      setSuccessMsg(`A 6-digit verification code has been sent to ${email.trim()}.`);
    } catch (err) {
      console.error("OTP send error:", err);
      const backendMessage =
        err.response?.data?.error?.message ||
        err.response?.data?.message ||
        (typeof err.response?.data?.error === "string" ? err.response.data.error : null) ||
        err.message ||
        "Failed to send verification code. Please check your details.";
      setErrorMsg(backendMessage);
    } finally {
      setLoading(false);
    }
  };

  // Resend OTP handler
  const handleResendOtp = async () => {
    if (!canResend || loading) return;
    setErrorMsg("");
    setLoading(true);
    try {
      await sendRegistrationOtp({
        name: name.trim(),
        email: email.trim(),
        mobile: mobile.trim(),
        password,
      });
      setResendTimer(60);
      setCanResend(false);
      setSuccessMsg("A new verification code has been dispatched to your email.");
    } catch (err) {
      setErrorMsg("Failed to resend code. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  // Step 2: Verify OTP and Complete Registration
  const handleVerifyAndRegister = async (e) => {
    e.preventDefault();
    setErrorMsg("");

    if (!otp || otp.trim().length !== 6) {
      setErrorMsg("Please enter the complete 6-digit verification code.");
      return;
    }

    setLoading(true);
    try {
      const res = await registerUser({
        name: name.trim(),
        email: email.trim(),
        mobile: mobile.trim(),
        password,
        otp: otp.trim(),
      });

      if (res && res.error) {
        const errorText = typeof res.error === "string" ? res.error : (res.error.message || "Registration failed");
        setErrorMsg(errorText);
        return;
      }

      // Requirement 2: Show "register successful" message
      alert("✅ register successful");
      navigate("/login");
    } catch (err) {
      console.error("Registration error:", err);
      const backendMessage =
        err.response?.data?.error?.message ||
        err.response?.data?.message ||
        (typeof err.response?.data?.error === "string" ? err.response.data.error : null) ||
        err.message ||
        "Invalid or expired verification code.";
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
          <h2>{step === 1 ? "Create Account" : "Verify Your Email"}</h2>
          <p>
            {step === 1
              ? "Join thousands of travelers enjoying luxury stays"
              : `Enter the 6-digit code sent to ${email}`}
          </p>
        </div>

        {errorMsg && (
          <div className="auth-alert-error" style={{ marginBottom: "16px" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <AlertCircle size={18} />
              <span>{errorMsg}</span>
            </div>
          </div>
        )}

        {successMsg && (
          <div className="auth-alert-success" style={{
            background: "rgba(34, 197, 94, 0.12)",
            border: "1px solid rgba(34, 197, 94, 0.3)",
            color: "#4ade80",
            padding: "10px 14px",
            borderRadius: "10px",
            marginBottom: "16px",
            fontSize: "0.88rem",
            display: "flex",
            alignItems: "center",
            gap: "8px"
          }}>
            <CheckCircle2 size={18} />
            <span>{successMsg}</span>
          </div>
        )}

        {/* STEP 1: Registration Details */}
        {step === 1 && (
          <form onSubmit={handleSendOtp} className="auth-form" noValidate>
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
                Email Address (@gmail.com) <span className="form-label-required">*</span>
              </label>
              <input
                id="reg-email"
                type="email"
                className={`auth-input ${fieldErrors.email ? "input-error" : ""}`}
                placeholder="e.g. rahul@gmail.com"
                value={email}
                onChange={(e) => { setEmail(e.target.value); if (fieldErrors.email) setFieldErrors({ ...fieldErrors, email: "" }); }}
                disabled={loading}
                required
              />
              {fieldErrors.email && <span className="field-error-text">{fieldErrors.email}</span>}
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
                placeholder="e.g. 9876543210"
                value={mobile}
                onChange={(e) => {
                  const val = e.target.value.replace(/\D/g, "");
                  setMobile(val);
                  if (fieldErrors.mobile) setFieldErrors({ ...fieldErrors, mobile: "" });
                }}
                disabled={loading}
                required
              />
              {fieldErrors.mobile && <span className="field-error-text">{fieldErrors.mobile}</span>}
            </div>

            <div className="form-group-item">
              <label className="form-label" htmlFor="reg-password">
                Password (min. 6 characters) <span className="form-label-required">*</span>
              </label>
              <div className="input-with-action">
                <input
                  id="reg-password"
                  type={showPassword ? "text" : "password"}
                  className={`auth-input ${fieldErrors.password ? "input-error" : ""}`}
                  placeholder="Create a strong password"
                  value={password}
                  onChange={(e) => { setPassword(e.target.value); if (fieldErrors.password) setFieldErrors({ ...fieldErrors, password: "" }); }}
                  disabled={loading}
                  required
                />
                <button
                  type="button"
                  className="password-toggle-btn"
                  onClick={() => setShowPassword(!showPassword)}
                  tabIndex={-1}
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
              <div className="input-with-action">
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
                  tabIndex={-1}
                  aria-label={showConfirmPassword ? "Hide confirm password" : "Show confirm password"}
                >
                  {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {fieldErrors.confirmPassword && <span className="field-error-text">{fieldErrors.confirmPassword}</span>}
            </div>

            <button type="submit" className="auth-btn-primary" disabled={loading} style={{ marginTop: "8px" }}>
              {loading ? "Sending Verification Code..." : "Register"}
            </button>
          </form>
        )}

        {/* STEP 2: Enter Email Verification OTP */}
        {step === 2 && (
          <form onSubmit={handleVerifyAndRegister} className="auth-form" noValidate>
            <div style={{
              textAlign: "center",
              margin: "12px 0 20px 0",
              background: "rgba(56, 189, 248, 0.08)",
              border: "1px dashed rgba(56, 189, 248, 0.3)",
              padding: "16px",
              borderRadius: "12px"
            }}>
              <ShieldCheck size={36} color="#38bdf8" style={{ margin: "0 auto 8px auto" }} />
              <div style={{ fontSize: "0.92rem", fontWeight: 600, color: "#f8fafc" }}>
                Check Your Email Inbox
              </div>
              <div style={{ fontSize: "0.82rem", color: "var(--text-muted, #94a3b8)", marginTop: "4px" }}>
                We sent a 6-digit code to <strong>{email}</strong>
              </div>
            </div>

            <div className="form-group-item">
              <label className="form-label" htmlFor="otp-input" style={{ textAlign: "center", display: "block" }}>
                Enter 6-Digit OTP Code
              </label>
              <input
                id="otp-input"
                type="text"
                maxLength={6}
                autoFocus
                className="auth-input"
                style={{
                  textAlign: "center",
                  letterSpacing: "8px",
                  fontSize: "1.4rem",
                  fontWeight: 800,
                  fontFamily: "monospace"
                }}
                placeholder="••••••"
                value={otp}
                onChange={(e) => setOtp(e.target.value.replace(/\D/g, ""))}
                disabled={loading}
                required
              />
            </div>

            <button type="submit" className="auth-btn-primary" disabled={loading} style={{ marginTop: "12px" }}>
              {loading ? "Verifying..." : "Verify & Complete Registration"}
            </button>

            <div style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginTop: "16px",
              fontSize: "0.85rem"
            }}>
              <button
                type="button"
                onClick={() => { setStep(1); setErrorMsg(""); setSuccessMsg(""); }}
                style={{
                  background: "none",
                  border: "none",
                  color: "var(--text-muted, #94a3b8)",
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  gap: "4px"
                }}
              >
                <ArrowLeft size={14} /> Edit Details
              </button>

              <button
                type="button"
                onClick={handleResendOtp}
                disabled={!canResend || loading}
                style={{
                  background: "none",
                  border: "none",
                  color: canResend ? "var(--accent-cyan, #38bdf8)" : "var(--text-muted, #64748b)",
                  cursor: canResend ? "pointer" : "not-allowed",
                  display: "flex",
                  alignItems: "center",
                  gap: "4px",
                  fontWeight: 600
                }}
              >
                <RefreshCw size={14} className={loading ? "spin-icon" : ""} />
                {canResend ? "Resend OTP" : `Resend in ${resendTimer}s`}
              </button>
            </div>
          </form>
        )}

        {/* Footer Links */}
        <div className="auth-footer" style={{ marginTop: "24px" }}>
          <p>
            Already have an account?{" "}
            <Link to="/login" className="auth-link-highlight">
              Log in here
            </Link>
          </p>

          <div className="auth-role-switch-row" style={{ marginTop: "16px", borderTop: "1px solid rgba(255,255,255,0.08)", paddingTop: "14px" }}>
            <Link to="/hotel-register" className="auth-role-switch-btn">
              <Building2 size={14} /> Register as Hotel Partner
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Register;
