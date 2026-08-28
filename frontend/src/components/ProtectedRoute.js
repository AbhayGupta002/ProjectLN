import React from "react";
import { Navigate } from "react-router-dom";

function ProtectedRoute({ children, allowedRoles }) {
  const userToken = localStorage.getItem("token");
  const hotelToken = localStorage.getItem("hotelToken");
  const adminToken = localStorage.getItem("adminToken");
  const currentRole = (localStorage.getItem("role") || "").toUpperCase();

  const isTokenExpired = (token) => {
    if (!token) return true;
    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      if (payload.exp && payload.exp * 1000 < Date.now()) {
        return true;
      }
      return false;
    } catch (e) {
      return true;
    }
  };

  // ADMIN ROUTE CHECK
  if (allowedRoles && allowedRoles.includes("ADMIN")) {
    if (!adminToken || isTokenExpired(adminToken) || currentRole !== "ADMIN") {
      localStorage.removeItem("adminToken");
      return <Navigate to="/admin-login" replace />;
    }
    return children;
  }

  // HOTEL ROUTE CHECK
  if (allowedRoles && allowedRoles.includes("HOTEL")) {
    if (!hotelToken || isTokenExpired(hotelToken) || currentRole !== "HOTEL") {
      localStorage.removeItem("hotelToken");
      return <Navigate to="/hotel-login" replace />;
    }
    return children;
  }

  // USER ROUTE CHECK
  if (allowedRoles && allowedRoles.includes("USER")) {
    if (!userToken || isTokenExpired(userToken) || (currentRole !== "USER" && currentRole !== "")) {
      localStorage.removeItem("token");
      return <Navigate to="/login" replace />;
    }
    return children;
  }

  // Fallback for shared authenticated routes
  const activeToken = userToken || hotelToken || adminToken;
  if (!activeToken || isTokenExpired(activeToken)) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default ProtectedRoute;
