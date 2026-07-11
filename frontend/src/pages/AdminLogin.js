import React, { useState, useRef, useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { adminLogin } from "../api/adminPanelApi"; // ✅ use API like user login
import "../styles/AdminLogin.css";

function AdminLogin() {

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  const emailRef = useRef(null);
  const navigate = useNavigate();

  // Autofocus email
  useEffect(() => {
    emailRef.current.focus();
  }, []);

  // Rain animation
  const rainDrops = useMemo(() => {
    return [...Array(150)].map(() => ({
      left: `${Math.random() * 100}%`,
      delay: `${Math.random() * 2}s`,
      duration: `${0.5 + Math.random() * 0.7}s`,
    }));
  }, []);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const res = await adminLogin({ email, password });

      console.log("✅ ADMIN LOGIN RESPONSE:", res);

      // ✅ SAME LOGIC AS USER LOGIN
      if (res.error) {
        setError(res.error.message || "Login failed");
        return;
      }

      const token = res.data;

      if (!token) {
        setError("Token not received from server");
        return;
      }

      // ✅ Save token
      localStorage.setItem("adminToken", token);
      try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        localStorage.setItem("email", payload.sub);
        localStorage.setItem("role", payload.role);
      } catch (e) {
        console.error("JWT Decode failed", e);
      }

      // ✅ Redirect
      navigate("/admin");

    } catch (err) {
      console.error(err);
      setError("Something went wrong");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="admin-login-page">

      {/* Rain */}
      <div className="rain">
        {rainDrops.map((drop, i) => (
          <div
            key={i}
            className="drop"
            style={{
              left: drop.left,
              animationDelay: drop.delay,
              animationDuration: drop.duration,
            }}
          ></div>
        ))}
      </div>

      {/* Glass Card */}
      <div className="glass-card">
        <h2 className="login-title">Admin Login</h2>

        <form onSubmit={handleLogin} className="login-form">

          <input
            ref={emailRef}
            type="email"
            placeholder="Enter admin email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <div className="password-wrapper">
            <input
              type={showPassword ? "text" : "password"}
              placeholder="Enter password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              onKeyDown={(e) => e.key === "Enter" && handleLogin(e)}
            />

            <span
              className="toggle-password"
              onClick={() => setShowPassword(!showPassword)}
            >
              {showPassword ? "🙈" : "👁️"}
            </span>
          </div>

          {error && <p className="error">{error}</p>}

          <button type="submit" disabled={loading}>
            {loading ? "Logging in..." : "Login"}
          </button>

        </form>
      </div>
    </div>
  );
}

export default AdminLogin;