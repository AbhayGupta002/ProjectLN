import React, { useState } from "react";
import { sendResetLink } from "../api/ForgotPasswordApi";

function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage(null);
    setError(null);

    if (!email) {
      setError("Please enter your email.");
      return;
    }

    setLoading(true);
    try {
      const res = await sendResetLink(email);
      // res might be a string or object depending on backend
      setMessage(typeof res === "string" ? res : (res.message || "Reset link sent."));
    } catch (err) {
      setError(err.message || "Failed to send reset link");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2>Forgot Password</h2>

      <form onSubmit={handleSubmit}>
        <div>
          <label>
            Email:
            <input
              type="email"
              placeholder="Enter your registered email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </label>
        </div>

        <div>
          <button type="submit" disabled={loading}>
            {loading ? "Sending..." : "Send Reset Link"}
          </button>
        </div>
      </form>

      {message && (
        <div>
          <strong>Success:</strong> {message}
        </div>
      )}

      {error && (
        <div>
          <strong>Error:</strong> {error}
        </div>
      )}
    </div>
  );
}

export default ForgotPassword;
