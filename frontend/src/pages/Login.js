import React, { useState } from "react";
import { loginUser } from "../api/authApi";
import { useNavigate } from "react-router-dom";
import "../styles/Login.css";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const res = await loginUser({ email, password });

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

      const token = res.data;  // ✔ Correct: backend returns token inside data

      if (!token) {
        alert("Token not received from server");
        return;
      }

      // Save token
      localStorage.setItem("token", token);
      try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        localStorage.setItem("email", payload.sub);
        localStorage.setItem("role", payload.role);
      } catch (e) {
        console.error("JWT Decode failed", e);
      }

      alert("✅ Login Successful!");
      navigate("/dashboard");

    } catch (err) {
      console.error(err);
      alert(err.response?.data?.error?.message || "Invalid credentials");
    }
  };

  return (
    <div className="page-container">
      <form onSubmit={handleSubmit}>
      <h2>User Login</h2>
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
                    onClick={() => navigate("/register")}
                  >
                    Register Here
                  </span>
                </p>

      </form>


      </div>
  );
}

export default Login;
