import React, { useState, useEffect, useRef } from "react";
import { Link } from "react-router-dom";
import "../styles/HomeNavbar.css";
import AIChatModal from "../components/AIChatModal";


function Navbar() {
  const [open, setOpen] = useState(false);  // ✅ FIXED
  const dropdownRef = useRef();
  const [openAI, setOpenAI] = useState(false);

  useEffect(() => {
    const handler = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

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
      {/* AI ASSISTANT BUTTON */}
      <div
              className="ai-assistant"
              title="Ask AI Assistant"
              onClick={() => setOpenAI(true)}
            >
              🤖
            </div>
            {openAI && <AIChatModal onClose={() => setOpenAI(false)} />}
    </nav>
  );
}

export default Navbar;
