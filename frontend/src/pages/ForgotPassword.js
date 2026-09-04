import React, { useState } from "react";
import { Link } from "react-router-dom";
import { Mail, ArrowLeft, CheckCircle2, AlertCircle, KeyRound } from "lucide-react";
import { sendResetLink } from "../api/ForgotPasswordApi";
import "../styles/Login.css";
import "../styles/ForgotPassword.css";

function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage(null);
    setError(null);

    const trimmedEmail = email.trim();
    if (!trimmedEmail) {
      setError("Please enter your registered email address.");
      return;
    }

    if (!trimmedEmail.toLowerCase().endsWith("@gmail.com")) {
      setError("Only official Google Mail (@gmail.com) is supported.");
      return;
    }

    setLoading(true);
    try {
      const res = await sendResetLink(trimmedEmail);
      const successMsg =
        (typeof res === "string" ? res : res.message) ||
        "Password has been sent to your registered email address.";
      setMessage(successMsg);
    } catch (err) {
      const errMsg = err.message || "Incorrect Details";
      setError(errMsg);
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
          <div style={{ display: "flex", justifyContent: "center", margin: "10px 0" }}>
            <div style={{
              background: "rgba(56, 189, 248, 0.15)",
              border: "1px solid rgba(56, 189, 248, 0.3)",
              padding: "12px",
              borderRadius: "50%",
              color: "#38bdf8"
            }}>
              <KeyRound size={26} />
            </div>
          </div>
          <h2>Reset Password</h2>
          <p>Enter your registered email and we'll generate and send a new password to your inbox.</p>
        </div>

        {/* Success Alert */}
        {message && (
          <div className="auth-alert-success" style={{
            background: "rgba(34, 197, 94, 0.12)",
            border: "1px solid rgba(34, 197, 94, 0.3)",
            color: "#4ade80",
            padding: "14px",
            borderRadius: "12px",
            marginBottom: "20px",
            fontSize: "0.9rem",
            display: "flex",
            alignItems: "flex-start",
            gap: "10px"
          }}>
            <CheckCircle2 size={20} style={{ flexShrink: 0, marginTop: "2px" }} />
            <div>
              <div style={{ fontWeight: 700, marginBottom: "4px" }}>Success!</div>
              <div>{message}</div>
              <div style={{ marginTop: "12px" }}>
                <Link to="/login" className="auth-btn-primary" style={{
                  display: "inline-block",
                  padding: "8px 18px",
                  textDecoration: "none",
                  fontSize: "0.85rem",
                  width: "auto"
                }}>
                  Proceed to Login
                </Link>
              </div>
            </div>
          </div>
        )}

        {/* Error Alert */}
        {error && (
          <div className="auth-alert-error" style={{
            background: "rgba(239, 68, 68, 0.12)",
            border: "1px solid rgba(239, 68, 68, 0.3)",
            color: "#f87171",
            padding: "12px 14px",
            borderRadius: "12px",
            marginBottom: "20px",
            fontSize: "0.88rem",
            display: "flex",
            alignItems: "center",
            gap: "10px"
          }}>
            <AlertCircle size={18} style={{ flexShrink: 0 }} />
            <span style={{ fontWeight: 600 }}>{error}</span>
          </div>
        )}

        {!message && (
          <form onSubmit={handleSubmit} className="auth-form" noValidate>
            <div className="form-group-item">
              <label>Registered Email Address</label>
              <div className="input-with-icon">
                <Mail className="field-icon" size={18} />
                <input
                  type="email"
                  placeholder="e.g. traveler@gmail.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={loading}
                  autoFocus
                  required
                />
              </div>
            </div>

            <button
              type="submit"
              className="auth-btn-primary"
              disabled={loading}
              style={{ marginTop: "10px" }}
            >
              {loading ? "Verifying & Sending..." : "Send Password to Mail"}
            </button>
          </form>
        )}

        <div className="auth-footer" style={{ marginTop: "24px", textAlign: "center" }}>
          <Link
            to="/login"
            style={{
              display: "inline-flex",
              alignItems: "center",
              gap: "6px",
              color: "var(--text-muted, #94a3b8)",
              textDecoration: "none",
              fontSize: "0.88rem",
              transition: "color 0.2s"
            }}
            onMouseOver={(e) => (e.currentTarget.style.color = "#38bdf8")}
            onMouseOut={(e) => (e.currentTarget.style.color = "var(--text-muted, #94a3b8)")}
          >
            <ArrowLeft size={16} /> Back to Login
          </Link>
        </div>
      </div>
    </div>
  );
}

export default ForgotPassword;
