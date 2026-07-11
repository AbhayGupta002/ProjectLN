import React, { useState, useEffect, useRef } from "react";
import { Link, useNavigate } from "react-router-dom";
import "../styles/HomeNavbar.css";
import AIChatModal from "../components/AIChatModal";
import { User, LogOut, Menu, X, MessageSquare, Compass, Shield } from "lucide-react";

function Navbar() {
  const [open, setOpen] = useState(false);
  const [openAI, setOpenAI] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const dropdownRef = useRef();
  const navigate = useNavigate();

  const token = localStorage.getItem("token") || localStorage.getItem("hotelToken") || localStorage.getItem("adminToken");
  const role = localStorage.getItem("role"); // User, Hotel, Admin
  const email = localStorage.getItem("email");

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
    localStorage.clear();
    setOpen(false);
    navigate("/login");
  };

  const getDashboardLink = () => {
    if (role === "Hotel") return "/hotel/dashboard";
    if (role === "Admin") return "/admin/dashboard";
    return "/dashboard";
  };

  return (
    <nav className="nav">
      <div className="nav-left">
        <Link to="/" className="logo">
          <Compass size={24} style={{ color: "#2563eb", strokeWidth: 2.5 }} />
          <span>LuxNes Travel</span>
        </Link>
        <div className={`header-links ${mobileMenuOpen ? "mobile-active" : ""}`}>
          <Link to="/" onClick={() => setMobileMenuOpen(false)}>Home</Link>
          <Link to="/flights" onClick={() => setMobileMenuOpen(false)}>Flights</Link>
          <Link to="/buses" onClick={() => setMobileMenuOpen(false)}>Buses</Link>
          <Link to="/trains" onClick={() => setMobileMenuOpen(false)}>Trains</Link>
          <Link to="/hotels" onClick={() => setMobileMenuOpen(false)}>Hotels</Link>
        </div>
      </div>

      <div className="nav-right">
        {/* AI ASSISTANT BUTTON */}
        <button
          className="ai-assistant-btn"
          title="Ask AI Assistant"
          onClick={() => setOpenAI(true)}
        >
          <MessageSquare size={18} />
          <span>AI Assistant</span>
        </button>

        {/* User Account / Login Dropdown */}
        {token ? (
          <div className="dropdown" ref={dropdownRef}>
            <button className="profile-btn" onClick={() => setOpen(!open)}>
              <User size={18} />
              <span className="profile-email">{email ? email.split("@")[0] : "Account"}</span>
            </button>
            {open && (
              <div className="dropdown-menu">
                <Link to={getDashboardLink()} onClick={() => setOpen(false)}>
                  <Shield size={16} /> My Dashboard
                </Link>
                <Link to="/update-profile" onClick={() => setOpen(false)}>
                  <User size={16} /> Edit Profile
                </Link>
                <button className="logout-btn" onClick={handleLogout}>
                  <LogOut size={16} /> Logout
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

        {/* Hamburger Menu Icon */}
        <button className="mobile-menu-toggle" onClick={() => setMobileMenuOpen(!mobileMenuOpen)}>
          {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </div>

      {openAI && <AIChatModal onClose={() => setOpenAI(false)} />}
    </nav>
  );
}

export default Navbar;
