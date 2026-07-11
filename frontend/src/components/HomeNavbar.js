import React, { useState, useEffect, useRef } from "react";
import { Link, useNavigate } from "react-router-dom";
import { MessageSquare, User, LogOut, Settings, HelpCircle } from "lucide-react";
import "../styles/HomeNavbar.css";
import AIChatModal from "../components/AIChatModal";

function Navbar() {
  const [open, setOpen] = useState(false);
  const [openAI, setOpenAI] = useState(false);
  const [email, setEmail] = useState("");
  const [role, setRole] = useState("");
  const dropdownRef = useRef();
  const navigate = useNavigate();

  // Detect login status from localStorage
  useEffect(() => {
    const userEmail = localStorage.getItem("email");
    const userRole = localStorage.getItem("role");
    if (userEmail) {
      setEmail(userEmail);
      setRole(userRole || "User");
    }
  }, []);

  // Handle outside clicks to close profile dropdown
  useEffect(() => {
    const handler = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("hotelToken");
    localStorage.removeItem("adminToken");
    localStorage.removeItem("email");
    localStorage.removeItem("role");
    setEmail("");
    setRole("");
    navigate("/");
    window.location.reload();
  };

  return (
    <nav className="nav">
      <div className="nav-left">
        <Link to="/" className="logo">Hotel-LuxNes</Link>
        <div className="header-links">
          <Link to="/view-hotels">view-hotels</Link>
          <Link to="/about-us">about-us</Link>
          <Link to="/contact-us">contact-us</Link>
          <Link to="/complaint">complaint</Link>
          <Link to="/feedback">feedback</Link>
        </div>
      </div>

      <div className="nav-search">
        <input type="text" placeholder="Search..." />
      </div>

      <div className="nav-right">
        {/* 🔥 AI ASSISTANT HEADER BUTTON */}
        <button
          className="ai-assistant-btn"
          title="Ask AI Assistant"
          onClick={() => setOpenAI(true)}
        >
          <MessageSquare size={16} />
          <span>AI Assistant</span>
        </button>

        {/* PROFILE OR AUTH BUTTONS */}
        {email ? (
          <div className="dropdown" ref={dropdownRef}>
            <button className="profile-btn" onClick={() => setOpen(!open)}>
              <User size={16} />
              <span className="profile-email">{email}</span>
            </button>

            {open && (
              <div className="dropdown-menu">
                <div style={{ padding: "10px 16px", fontSize: "0.8rem", color: "#64748b", borderBottom: "1px solid #f1f5f9" }}>
                  Logged in as: <strong>{role}</strong>
                </div>
                <Link to={role === "ADMIN" ? "/admin" : role === "Hotel" ? "/hotel-login-dashboard" : "/dashboard"} onClick={() => setOpen(false)}>
                  <Settings size={14} /> My Dashboard
                </Link>
                <button onClick={handleLogout} className="logout-btn">
                  <LogOut size={14} /> Logout
                </button>
              </div>
            )}
          </div>
        ) : (
          <div className="auth-buttons">
            <Link to="/login" className="login-nav-btn">Login</Link>
            <Link to="/register" className="register-nav-btn">Sign Up</Link>
          </div>
        )}
      </div>

      {openAI && <AIChatModal onClose={() => setOpenAI(false)} />}
    </nav>
  );
}

export default Navbar;
