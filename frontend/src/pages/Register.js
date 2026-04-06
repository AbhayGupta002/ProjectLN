import React, { useState } from "react";
import { registerUser } from "../api/authApi";
import { useNavigate } from "react-router-dom";


function Register() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [mobile, setMobile] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const res = await registerUser({ name, email, mobile, password });

      alert("✅ Registration successful!");
      console.log("Success:", res);

    } catch (err) {
      console.log("Error:", err.response?.data);

      // ⭐ FIXED ERROR HANDLING ⭐
      const backendMessage =
        err.response?.data?.error?.message ||      // if backend sends: response.setError(error)
        err.response?.data?.message ||             // if backend sends: message field
        "❌ Registration failed";

      alert(backendMessage);
    }
  };

  return (
    <div className="page-container">

      <form onSubmit={handleSubmit}>
      <h2>User Register</h2>

        <input type="text" placeholder="Name" value={name}
               onChange={e => setName(e.target.value)} required />

        <input type="email" placeholder="Email" value={email}
               onChange={e => setEmail(e.target.value)} required
               onBlur={() => {
                if (!email.endsWith("@gmail.com")) {
                  alert("Only Gmail addresses are allowed!");
                  setEmail("");
                }
              }}/>

        <input type="mobile" placeholder="Number" value={mobile}
               onChange={e => setMobile(e.target.value)}minLength={10} maxLength={13} required />

        <input type="password" placeholder="Password" value={password}
               onChange={e => setPassword(e.target.value)} minLength={5} maxLength={15} required />
               <p style={{ marginTop: "10px", fontSize: "18px", color: "white" }}>
                                 already have an account?{" "}
                                 <span
                                   style={{ color: "#00aaff", cursor: "pointer" }}
                                   onClick={() => navigate("/login")}
                                 >
                                   login here
                                 </span>
                               </p>


        <button type="submit">Register</button>
      </form>
    </div>
  );
}

export default Register;
