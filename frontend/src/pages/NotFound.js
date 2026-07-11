import React from "react";
import { useNavigate } from "react-router-dom";
import HomeNavbar from "../components/HomeNavbar";
import { Compass } from "lucide-react";

function NotFound() {
  const navigate = useNavigate();

  return (
    <div style={{ backgroundColor: "#f8fafc", minHeight: "100vh", display: "flex", flexDirection: "column" }}>
      <HomeNavbar />
      <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: 20, textAlign: "center", fontFamily: "'Inter', sans-serif" }}>
        <Compass size={64} style={{ color: "#2563eb", marginBottom: 20, animation: "spin 12s linear infinite" }} />
        <h1 style={{ fontSize: "5rem", fontWeight: 900, color: "#1e293b", margin: "0 0 10px 0" }}>404</h1>
        <h2 style={{ fontSize: "1.75rem", fontWeight: 700, color: "#0f172a", margin: "0 0 15px 0" }}>Page Not Found</h2>
        <p style={{ color: "#64748b", maxWidth: 450, margin: "0 0 30px 0", lineHeight: 1.6 }}>
          The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.
        </p>
        <button
          onClick={() => navigate("/")}
          style={{
            backgroundColor: "#2563eb",
            color: "white",
            border: "none",
            padding: "12px 28px",
            borderRadius: "10px",
            fontSize: "1rem",
            fontWeight: 600,
            cursor: "pointer",
            boxShadow: "0 4px 6px -1px rgba(37, 99, 235, 0.2)",
            transition: "all 0.2s ease"
          }}
          onMouseOver={(e) => { e.currentTarget.style.backgroundColor = "#1d4ed8"; }}
          onMouseOut={(e) => { e.currentTarget.style.backgroundColor = "#2563eb"; }}
        >
          Go Back Home
        </button>
      </div>
      
      <style>{`
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
}

export default NotFound;
