import React from "react";
import { Navigate } from "react-router-dom";

function ProtectedRoute({ children }) {
  const token = localStorage.getItem("token") || localStorage.getItem("hotelToken") || localStorage.getItem("adminToken");

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default ProtectedRoute;
