import React from "react";
import "./index.css";
import { BrowserRouter as Router, Routes, Route, Navigate} from "react-router-dom";

import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import BookHotel from "./pages/BookHotel";
import Dashboard from "./pages/Dashboard";
import HotelLogin from "./pages/HotelLogin";
import Navbar from "./components/HomeNavbar";
import UpdateProfile from "./pages/UpdateProfile";
import HotelRegister from "./pages/HotelRegister";
import ForgotPassword from "./pages/ForgotPassword";
import ComplaintList from "./components/ComplaintList";
import ComplaintForm from "./components/ComplaintForm";
import HotelTourCreate from "./components/HotelCreateTour";
import HotelLoginDashboard from "./pages/HotelLoginDashboard"
import HotelProfile from "./pages/HotelLoginDashboard";
import AdminDashboard from "./pages/AdminDashboard";
import AdminLogin from "./pages/AdminLogin";

function App() {
const token = localStorage.getItem("adminToken");
  return (
    <Router>
      <Navbar />

      <Routes>
        {/* Home */}
        <Route path="/" element={<Home />} />

        {/* User Auth */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        {/* Hotel Auth */}
        <Route path="/hotel-login" element={<HotelLogin />} />
        <Route path="/hotel-register" element={<HotelRegister />} />
        {/* Protected Pages */}
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/book-hotel" element={<BookHotel />} />
        <Route path="/update-profile" element={<UpdateProfile />} />
        <Route path="/complaint" element={<ComplaintForm />} />
        <Route path="/my-complaints" element={<ComplaintList userId={1} />} />

       <Route path="/hotel-login-dashboard" element={<HotelLoginDashboard />}/>
      <Route path="/hotel/create-tour" element={<HotelTourCreate />} />
      <Route path="/hotel-profile" element={<HotelProfile />} />
      <Route path="/hotel/update-profile" element={<UpdateProfile />} />


 <Route path="/admin-login" element={<AdminLogin />} />

 <Route
   path="/admin"
   element={
     localStorage.getItem("adminToken")
       ? <AdminDashboard />
       : <Navigate to="/admin-login" />
   }
 />

 <Route path="*" element={<Navigate to="/admin-login" />} />





      </Routes>
    </Router>
  );
}

export default App;

