import React, { useState } from "react";
import { loginUser, verify2fa } from "../api/authApi";
import { useNavigate } from "react-router-dom";
import "../styles/Login.css";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [otp, setOtp] = useState("");
  const [step2fa, setStep2fa] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");
  const [statusMsg, setStatusMsg] = useState("");
  const [isLocked, setIsLocked] = useState(false);
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

  const handleCredentialsSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg("");
    setStatusMsg("");
    setIsLocked(false);
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
      const token = typeof res.data === "string" ? res.data : null;
      if (!token) {
        setErrorMsg("Authentication token not received from server");
        return;
      }

      handleAuthSuccess(token);

    } catch (err) {
      console.error("Login error:", err);
      if (err.response?.status === 423) {
        setIsLocked(true);
        setErrorMsg(
          err.response?.data?.error?.message ||
          "Account locked due to 4 consecutive failed password attempts. Please use Forgot Password to unlock."
        );
      } else {
        const msg =
          err.response?.data?.error?.message ||
          err.response?.data?.message ||
          "Invalid email or password";
        setErrorMsg(msg);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleOtpSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg("");
    setLoading(true);

    try {
      const res = await verify2fa(email, otp);

      if (res.error) {
        setErrorMsg(res.error.message || "Invalid or expired OTP");
        return;
      }

      const token = typeof res.data === "string" ? res.data : null;
      if (!token) {
        setErrorMsg("Token not received upon OTP verification");
        return;
      }

      handleAuthSuccess(token);

    } catch (err) {
      console.error("2FA error:", err);
      const msg =
        err.response?.data?.error?.message ||
        err.response?.data?.message ||
        "Invalid or expired OTP";
      setErrorMsg(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      {!step2fa ? (
        <form onSubmit={handleCredentialsSubmit}>
          <h2>User Login</h2>

          {statusMsg && <p style={{ color: "#38bdf8", fontSize: "14px", marginBottom: "12px" }}>{statusMsg}</p>}
          {errorMsg && (
            <div style={{ background: "rgba(239, 68, 68, 0.15)", border: "1px solid rgba(239, 68, 68, 0.3)", borderRadius: "8px", padding: "10px", marginBottom: "14px" }}>
              <p style={{ color: "#f87171", fontSize: "14px", margin: 0 }}>{errorMsg}</p>
              {isLocked && (
                <button
                  type="button"
                  onClick={() => navigate("/forgot-password")}
                  style={{ marginTop: "8px", padding: "6px 12px", background: "#ef4444", color: "#fff", border: "none", borderRadius: "6px", fontSize: "12px", cursor: "pointer" }}
                >
                  Unlock via Forgot Password
                </button>
              )}
            </div>
          )}

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

          <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: "12px", marginTop: "-6px" }}>
            <span
              style={{ color: "#38bdf8", cursor: "pointer", fontSize: "13px" }}
              onClick={() => navigate("/forgot-password")}
            >
              Forgot Password?
            </span>
          </div>

          <button type="submit" disabled={loading}>
            {loading ? "Verifying..." : "Login"}
          </button>

          <p style={{ marginTop: "15px", fontSize: "15px", color: "#cbd5e1" }}>
            Don’t have an account?{" "}
            <span
              style={{ color: "#38bdf8", cursor: "pointer", fontWeight: 600 }}
              onClick={() => navigate("/register")}
            >
              Register Here
            </span>
          </p>

          <div style={{ marginTop: "20px", paddingTop: "15px", borderTop: "1px solid rgba(255,255,255,0.1)", display: "flex", justifyContent: "space-between" }}>
            <span
              style={{ color: "#94a3b8", cursor: "pointer", fontSize: "13px" }}
              onClick={() => navigate("/hotel-login")}
            >
              🏨 Hotel Login
            </span>
            <span
              style={{ color: "#94a3b8", cursor: "pointer", fontSize: "13px" }}
              onClick={() => navigate("/admin-login")}
            >
              🛡️ Admin Login
            </span>
          </div>
        </form>
      ) : (
        <form onSubmit={handleOtpSubmit}>
          <h2>Two-Factor Authentication</h2>
          <p style={{ color: "#94a3b8", fontSize: "14px", marginBottom: "15px" }}>
            Enter the 6-digit OTP sent to <strong>{email}</strong>
          </p>

          {statusMsg && <p style={{ color: "#38bdf8", fontSize: "13px", marginBottom: "12px", background: "rgba(56,189,248,0.1)", padding: "8px", borderRadius: "6px" }}>{statusMsg}</p>}
          {errorMsg && <p style={{ color: "#f87171", fontSize: "14px", marginBottom: "12px" }}>{errorMsg}</p>}

          <input
            type="text"
            placeholder="Enter 6-digit OTP"
            value={otp}
            onChange={(e) => setOtp(e.target.value)}
            maxLength={6}
            required
            autoFocus
          />

          <button type="submit" disabled={loading}>
            {loading ? "Verifying OTP..." : "Verify & Complete Login"}
          </button>

          <p style={{ marginTop: "15px", fontSize: "14px", color: "#94a3b8" }}>
            <span
              style={{ color: "#38bdf8", cursor: "pointer" }}
              onClick={() => { setStep2fa(false); setOtp(""); setErrorMsg(""); setStatusMsg(""); }}
            >
              ← Back to password login
            </span>
          </p>
        </form>
      )}
    </div>
  );
}

export default Login;
