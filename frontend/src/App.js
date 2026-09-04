import React from "react";
import "./index.css";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import BookHotel from "./pages/BookHotel";
import Dashboard from "./pages/Dashboard";
import HotelLogin from "./pages/HotelLogin";
import UpdateProfile from "./pages/UpdateProfile";
import HotelRegister from "./pages/HotelRegister";
import ForgotPassword from "./pages/ForgotPassword";
import ComplaintList from "./components/ComplaintList";
import ComplaintForm from "./components/ComplaintForm";
import HotelTourCreate from "./components/HotelCreateTour";
import HotelLoginDashboard from "./pages/HotelLoginDashboard";
import HotelProfile from "./pages/HotelLoginDashboard";
import AdminDashboard from "./pages/AdminDashboard";
import AdminLogin from "./pages/AdminLogin";

// New Booking Pages
import FlightBooking from "./pages/FlightBooking";
import BusBooking from "./pages/BusBooking";
import TrainBooking from "./pages/TrainBooking";

// Protected Route & 404
import ProtectedRoute from "./components/ProtectedRoute";
import NotFound from "./pages/NotFound";
import FixedAIAgent from "./components/FixedAIAgent";

function App() {
  React.useEffect(() => {
    // Proactively warm up Render backend in the background to eliminate cold starts
    const apiBase = process.env.REACT_APP_API_URL || "https://world-tour-app-onc4.onrender.com";
    fetch(`${apiBase}/api/health`, { method: "GET", keepalive: true }).catch(() => {});

    // Periodic heartbeat every 10 minutes while active in browser
    const heartbeat = setInterval(() => {
      fetch(`${apiBase}/api/health`, { method: "GET", keepalive: true }).catch(() => {});
    }, 10 * 60 * 1000);

    return () => clearInterval(heartbeat);
  }, []);

  return (
    <Router>
      <FixedAIAgent />
      <Routes>
        {/* Home & Public Booking Searches */}
        <Route path="/" element={<Home />} />
        <Route path="/flights" element={<FlightBooking />} />
        <Route path="/buses" element={<BusBooking />} />
        <Route path="/trains" element={<TrainBooking />} />

        {/* User Auth */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />

        {/* Hotel Auth */}
        <Route path="/hotel-login" element={<HotelLogin />} />
        <Route path="/hotel-register" element={<HotelRegister />} />

        {/* User Protected Pages */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute allowedRoles={["USER"]}>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/update-profile"
          element={
            <ProtectedRoute allowedRoles={["USER", "HOTEL"]}>
              <UpdateProfile />
            </ProtectedRoute>
          }
        />
        <Route
          path="/book-hotel"
          element={
            <ProtectedRoute allowedRoles={["USER"]}>
              <BookHotel />
            </ProtectedRoute>
          }
        />
        <Route
          path="/complaint"
          element={
            <ProtectedRoute allowedRoles={["USER"]}>
              <ComplaintForm />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-complaints"
          element={
            <ProtectedRoute allowedRoles={["USER"]}>
              <ComplaintList userId={1} />
            </ProtectedRoute>
          }
        />

        {/* Hotel Protected Pages */}
        <Route
          path="/hotel-login-dashboard"
          element={
            <ProtectedRoute allowedRoles={["HOTEL"]}>
              <HotelLoginDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/hotel/create-tour"
          element={
            <ProtectedRoute allowedRoles={["HOTEL"]}>
              <HotelTourCreate />
            </ProtectedRoute>
          }
        />
        <Route
          path="/hotel-profile"
          element={
            <ProtectedRoute allowedRoles={["HOTEL"]}>
              <HotelProfile />
            </ProtectedRoute>
          }
        />
        <Route
          path="/hotel/update-profile"
          element={
            <ProtectedRoute allowedRoles={["HOTEL"]}>
              <UpdateProfile />
            </ProtectedRoute>
          }
        />

        {/* Admin Pages */}
        <Route path="/admin-login" element={<AdminLogin />} />
        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <AdminDashboard />
            </ProtectedRoute>
          }
        />

        {/* Wildcard 404 Route */}
        <Route path="*" element={<NotFound />} />
      </Routes>
    </Router>
  );
}

export default App;
