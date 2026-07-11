import axios from "axios";
import React, { useEffect, useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { User, CreditCard, Hotel as HotelIcon, Plane, Bus, Train, Settings, History, HelpCircle, MessageSquare, LogOut } from "lucide-react";
import { getUserFlightBookings, cancelFlightBooking } from "../api/flightBookingApi";
import { getUserBusBookings, cancelBusBooking } from "../api/busBookingApi";
import { getUserTrainBookings, cancelTrainBooking } from "../api/trainBookingApi";
import "../styles/Dashboard.css";
import AIChatModal from "../components/AIChatModal";

const API_BASE = process.env.REACT_APP_API_URL || `${process.env.REACT_APP_API_URL || "http://localhost:8080"}`;

function Dashboard() {
  const [openPanel, setOpenPanel] = useState("overview"); 
  const navigate = useNavigate();
  const dropdownRef = useRef();
  const [showProfilePanel, setShowProfilePanel] = useState(false);
  const [openAI, setOpenAI] = useState(false);

  /* ------------------- STATE ------------------- */
  const [profile, setProfile] = useState(null);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const [activeTab, setActiveTab] = useState("overview");
  const [bookingTab, setBookingTab] = useState("hotels"); // hotels, flights, buses, trains
  
  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState({});
  const [saving, setSaving] = useState(false);
  const [passwordForm, setPasswordForm] = useState({ currentPassword: "", newPassword: "", confirmPassword: "" });
  const [open, setOpen] = useState(false);
  
  // Bookings list state
  const [bookings, setBookings] = useState([]); // hotels
  const [flightBookings, setFlightBookings] = useState([]);
  const [busBookings, setBusBookings] = useState([]);
  const [trainBookings, setTrainBookings] = useState([]);

  /* ------------------- LOAD PROFILE & DATA ------------------- */
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) { navigate("/login"); return; }

    const fetchProfile = async () => {
      try {
        const res = await axios.get(`${API_BASE}/api/dashboard/profile`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setProfile(res.data);
        setEditForm(res.data);
        setUser(res.data);
        
        // Load bookings if userId exists
        if (res.data && res.data.id) {
          fetchUserAllBookings(res.data.id, token);
        }
      } catch (err) {
        console.error(err);
        navigate("/login");
      } finally { setLoading(false); }
    };

    fetchProfile();
  }, [navigate]);

  const fetchUserAllBookings = async (userId, token) => {
    try {
      // Hotel bookings
      const hotelRes = await axios.get(`${API_BASE}/api/bookings/getuserbookings?email=${localStorage.getItem("email")}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setBookings(hotelRes.data?.data || hotelRes.data || []);
      
      // Flight bookings
      const flightRes = await getUserFlightBookings(userId);
      setFlightBookings(flightRes.data?.data || []);

      // Bus bookings
      const busRes = await getUserBusBookings(userId);
      setBusBookings(busRes.data?.data || []);

      // Train bookings
      const trainRes = await getUserTrainBookings(userId);
      setTrainBookings(trainRes.data?.data || []);
    } catch (err) {
      console.error("Error loading bookings:", err);
    }
  };

  /* ------------------- HANDLERS ------------------- */
  const handleEditToggle = () => { if (isEditing) setEditForm(profile); setIsEditing(!isEditing); };
  const handleInputChange = (e) => setEditForm({ ...editForm, [e.target.name]: e.target.value });

  const handleSaveProfile = async () => {
    setSaving(true);
    try {
      const token = localStorage.getItem("token");
      const res = await axios.put(`${API_BASE}/api/dashboard/update-profile`, editForm, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setProfile(res.data);
      setIsEditing(false);
      alert("Profile updated!");
    } catch (err) { alert("Failed to update profile"); }
    finally { setSaving(false); }
  };

  const handleLogout = () => { localStorage.clear(); navigate("/login"); };

  const handleCancelFlight = async (id) => {
    if (window.confirm("Are you sure you want to cancel this flight booking?")) {
      try {
        const res = await cancelFlightBooking(id);
        if (res.data?.success) {
          alert("Flight booking cancelled successfully.");
          if (user?.id) fetchUserAllBookings(user.id, localStorage.getItem("token"));
        }
      } catch (err) {
        console.error(err);
        alert("Cancellation failed.");
      }
    }
  };

  const handleCancelBus = async (id) => {
    if (window.confirm("Are you sure you want to cancel this bus booking?")) {
      try {
        const res = await cancelBusBooking(id);
        if (res.data?.success) {
          alert("Bus booking cancelled successfully.");
          if (user?.id) fetchUserAllBookings(user.id, localStorage.getItem("token"));
        }
      } catch (err) {
        console.error(err);
        alert("Cancellation failed.");
      }
    }
  };

  const handleCancelTrain = async (id) => {
    if (window.confirm("Are you sure you want to cancel this train booking?")) {
      try {
        const res = await cancelTrainBooking(id);
        if (res.data?.success) {
          alert("Train booking cancelled successfully.");
          if (user?.id) fetchUserAllBookings(user.id, localStorage.getItem("token"));
        }
      } catch (err) {
        console.error(err);
        alert("Cancellation failed.");
      }
    }
  };

  const handleCancelHotel = async (id) => {
    if (window.confirm("Are you sure you want to cancel this hotel stay booking?")) {
      try {
        const token = localStorage.getItem("token");
        const res = await axios.put(`${API_BASE}/api/bookings/cancel/${id}`, {}, {
          headers: { Authorization: `Bearer ${token}` }
        });
        if (res.data?.data?.success || res.data?.success) {
          alert("Hotel booking cancelled successfully.");
          if (user?.id) fetchUserAllBookings(user.id, token);
        }
      } catch (err) {
        console.error(err);
        alert("Cancellation failed.");
      }
    }
  };

  const getAllTransactions = () => {
    const list = [];
    
    // 1. Hotels
    bookings.forEach(b => {
      list.push({
        id: `TXN-HTL-${b.id || 100 + Math.floor(Math.random() * 900)}`,
        type: "Hotel Stay",
        details: b.hotelName || `Hotel Booking (Room ID: ${b.hotelId || "Standard"})`,
        date: b.checkIn || "N/A",
        amount: b.amount || 2500,
        status: "CONFIRMED"
      });
    });

    // 2. Flights
    flightBookings.forEach(b => {
      list.push({
        id: `TXN-FLT-${b.id || 200 + Math.floor(Math.random() * 900)}`,
        type: "Flight Ticket",
        details: `Flight: ${b.flightId} | Passenger: ${b.passengerName}`,
        date: b.journeyDate || "N/A",
        amount: b.totalFare || 0,
        status: b.bookingStatus || "CONFIRMED"
      });
    });

    // 3. Buses
    busBookings.forEach(b => {
      list.push({
        id: `TXN-BUS-${b.id || 300 + Math.floor(Math.random() * 900)}`,
        type: "Bus Ticket",
        details: `Bus ID: ${b.busId} | Passenger: ${b.passengerName}`,
        date: b.journeyDate || "N/A",
        amount: b.totalFare || 0,
        status: b.bookingStatus || "CONFIRMED"
      });
    });

    // 4. Trains
    trainBookings.forEach(b => {
      list.push({
        id: `TXN-TRN-${b.id || 400 + Math.floor(Math.random() * 900)}`,
        type: "Train Ticket",
        details: `Train ID: ${b.trainId} | Passenger: ${b.passengerName}`,
        date: b.journeyDate || "N/A",
        amount: b.totalFare || 0,
        status: b.bookingStatus || "CONFIRMED"
      });
    });

    return list;
  };

  /* ------------------- RENDER ------------------- */
  if (loading) return <p style={{ padding: 40, fontFamily: "Inter, sans-serif" }}>Loading your travel dashboard...</p>;
  if (!profile) return <p style={{ padding: 40, fontFamily: "Inter, sans-serif" }}>No profile found.</p>;

  return (
    <div className="dashboard sidebar-open">
      {/* SIDEBAR */}
      <aside className="sidebar">
        <div className="sidebar-top">
          <div className="sidebar-logo">LuxNes Client</div>
        </div>
        <nav>
          <ul>
            <li className={openPanel === "overview" ? "active-link" : ""} onClick={() => { setOpenPanel("overview"); setShowProfilePanel(false); }}>
              <CreditCard size={18} /> Overview
            </li>
            <li className={openPanel === "bookings" ? "active-link" : ""} onClick={() => { setOpenPanel("bookings"); setShowProfilePanel(false); }}>
              <HotelIcon size={18} /> My Bookings
            </li>
            <li className={openPanel === "transactions" ? "active-link" : ""} onClick={() => { setOpenPanel("transactions"); setShowProfilePanel(false); }}>
              <History size={18} /> Transactions
            </li>
            <li onClick={() => setShowProfilePanel(true)}>
              <Settings size={18} /> Edit Profile
            </li>
            <li onClick={() => setOpenAI(true)} style={{ color: "#a855f7", fontWeight: "bold" }}>
              <MessageSquare size={18} /> AI Travel Agent
            </li>
            <li onClick={handleLogout} style={{ color: "#ef4444", marginTop: 40 }}>
              <LogOut size={18} /> Logout
            </li>
          </ul>
        </nav>
      </aside>

      {/* MAIN CONTENT */}
      <main className="main-content">
        <header className="header">
          <h1>Welcome, {user ? user.name : "Traveler"}</h1>
          <div className="profile-section">
            <img src={"https://api.dicebear.com/7.x/adventurer/svg?seed=" + (user?.name || "LuxNes")} alt="Avatar" className="avatar" />
            <span>{user?.email || "Guest"}</span>
          </div>
        </header>

        <div className="main-grid">
          <div className="col-left" style={{ gridColumn: "1/-1" }}>
            
            {/* EDIT PROFILE PANEL */}
            {showProfilePanel && (
              <div className="glass-card" style={{ marginBottom: 30 }}>
                <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 20 }}>
                  <h3>Edit Profile Details</h3>
                  <button className="cancel-btn" style={{ padding: "6px 12px", width: "auto" }} onClick={() => setShowProfilePanel(false)}>Close</button>
                </div>

                {!isEditing ? (
                  <button onClick={handleEditToggle} className="edit-btn">Edit Details</button>
                ) : (
                  <div style={{ display: "flex", gap: 10, marginBottom: 15 }}>
                    <button onClick={handleSaveProfile} className="save-btn">{saving ? "Saving..." : "Save"}</button>
                    <button onClick={handleEditToggle} className="cancel-btn" style={{ width: "auto" }}>Cancel</button>
                  </div>
                )}

                {["name", "mobile", "city"].map(f => (
                  <div className="form-group" key={f} style={{ marginBottom: 15 }}>
                    <label style={{ fontWeight: 600, display: "block", marginBottom: 6 }}>{f.charAt(0).toUpperCase() + f.slice(1)}:</label>
                    <input
                      name={f}
                      value={editForm[f] || ""}
                      disabled={!isEditing}
                      onChange={handleInputChange}
                      style={{ padding: 10, width: "100%", borderRadius: 8, border: "1px solid #cbd5e1" }}
                    />
                  </div>
                ))}

                <div className="form-group">
                  <label style={{ fontWeight: 600, display: "block", marginBottom: 6 }}>Email:</label>
                  <input value={editForm.email || ""} disabled style={{ padding: 10, width: "100%", borderRadius: 8, border: "1px solid #cbd5e1", backgroundColor: "#f1f5f9" }} />
                </div>
              </div>
            )}

            {/* MY BOOKINGS TABS & LISTINGS */}
            {openPanel === "bookings" && (
              <div className="glass-card">
                <div style={{ display: "flex", justifyContent: "space-between", flexWrap: "wrap", gap: 15, borderBottom: "1px solid #e2e8f0", paddingBottom: 15, marginBottom: 25 }}>
                  <h2>My Active Bookings</h2>
                  
                  {/* Category Switcher Tabs */}
                  <div className="booking-subtabs" style={{ display: "flex", gap: 10 }}>
                    <button className={`subtab-btn ${bookingTab === "hotels" ? "active" : ""}`} onClick={() => setBookingTab("hotels")}>
                      <HotelIcon size={16} /> Hotels
                    </button>
                    <button className={`subtab-btn ${bookingTab === "flights" ? "active" : ""}`} onClick={() => setBookingTab("flights")}>
                      <Plane size={16} /> Flights
                    </button>
                    <button className={`subtab-btn ${bookingTab === "buses" ? "active" : ""}`} onClick={() => setBookingTab("buses")}>
                      <Bus size={16} /> Buses
                    </button>
                    <button className={`subtab-btn ${bookingTab === "trains" ? "active" : ""}`} onClick={() => setBookingTab("trains")}>
                      <Train size={16} /> Trains
                    </button>
                  </div>
                </div>

                {/* Hotels Bookings */}
                {bookingTab === "hotels" && (
                  <div className="booking-list">
                    {bookings.length === 0 ? <p style={{ color: "#64748b" }}>No hotel bookings found.</p> :
                      bookings.map((b, i) => (
                        <div className="booking-item-card" key={i}>
                          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", width: "100%" }}>
                            <div>
                              <h4>{b.hotel?.hotel || b.hotelName || "LuxNes Premium Stay"}</h4>
                              <p style={{ margin: "4px 0", color: "#64748b" }}>City: {b.hotel?.city || "N/A"} | Price: ₹{b.amount || "N/A"}</p>
                              <p style={{ margin: "4px 0", color: "#64748b" }}>
                                Check-in: {b.checkIn ? b.checkIn.split("T")[0] : "N/A"} | Check-out: {b.checkOut ? b.checkOut.split("T")[0] : "N/A"}
                              </p>
                              <p style={{ fontSize: "0.85rem" }}>
                                Status: <span style={{ color: b.bookingStatus === "CANCELLED" ? "#ef4444" : "#10b981", fontWeight: 700 }}>
                                  {b.bookingStatus || "CONFIRMED"}
                                </span>
                              </p>
                            </div>
                            {b.bookingStatus !== "CANCELLED" && (
                              <button className="cancel-booking-btn" onClick={() => handleCancelHotel(b.id)}>
                                Cancel Booking
                              </button>
                            )}
                          </div>
                        </div>
                      ))
                    }
                  </div>
                )}

                {/* Flights Bookings */}
                {bookingTab === "flights" && (
                  <div className="booking-list">
                    {flightBookings.length === 0 ? <p style={{ color: "#64748b" }}>No flight bookings found.</p> :
                      flightBookings.map((b, i) => (
                        <div className="booking-item-card" key={i}>
                          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", width: "100%" }}>
                            <div>
                              <h4>Passenger: {b.passengerName} ({b.passengerGender})</h4>
                              <p style={{ margin: "4px 0" }}>Flight ID: {b.flightId} | Seats: {b.numberOfSeats}</p>
                              <p style={{ margin: "4px 0", color: "#64748b" }}>Journey Date: {b.journeyDate} | Fare: ₹{b.totalFare}</p>
                              <p style={{ fontSize: "0.85rem" }}>
                                Status: <span style={{ color: b.bookingStatus === "CANCELLED" ? "#ef4444" : "#10b981", fontWeight: 700 }}>
                                  {b.bookingStatus}
                                </span>
                              </p>
                            </div>
                            {b.bookingStatus !== "CANCELLED" && (
                              <button className="cancel-booking-btn" onClick={() => handleCancelFlight(b.id)}>
                                Cancel Flight
                              </button>
                            )}
                          </div>
                        </div>
                      ))
                    }
                  </div>
                )}

                {/* Buses Bookings */}
                {bookingTab === "buses" && (
                  <div className="booking-list">
                    {busBookings.length === 0 ? <p style={{ color: "#64748b" }}>No bus bookings found.</p> :
                      busBookings.map((b, i) => (
                        <div className="booking-item-card" key={i}>
                          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", width: "100%" }}>
                            <div>
                              <h4>Passenger: {b.passengerName}</h4>
                              <p style={{ margin: "4px 0" }}>Bus ID: {b.busId} | Seats: {b.numberOfSeats}</p>
                              <p style={{ margin: "4px 0", color: "#64748b" }}>Journey Date: {b.journeyDate} | Fare: ₹{b.totalFare}</p>
                              <p style={{ fontSize: "0.85rem" }}>
                                Status: <span style={{ color: b.bookingStatus === "CANCELLED" ? "#ef4444" : "#10b981", fontWeight: 700 }}>
                                  {b.bookingStatus}
                                </span>
                              </p>
                            </div>
                            {b.bookingStatus !== "CANCELLED" && (
                              <button className="cancel-booking-btn" onClick={() => handleCancelBus(b.id)}>
                                Cancel Seat
                              </button>
                            )}
                          </div>
                        </div>
                      ))
                    }
                  </div>
                )}

                {/* Trains Bookings */}
                {bookingTab === "trains" && (
                  <div className="booking-list">
                    {trainBookings.length === 0 ? <p style={{ color: "#64748b" }}>No train bookings found.</p> :
                      trainBookings.map((b, i) => (
                        <div className="booking-item-card" key={i}>
                          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", width: "100%" }}>
                            <div>
                              <h4>Passenger: {b.passengerName}</h4>
                              <p style={{ margin: "4px 0" }}>Train ID: {b.trainId} | Tickets: {b.numberOfSeats}</p>
                              <p style={{ margin: "4px 0", color: "#64748b" }}>Journey Date: {b.journeyDate} | Fare: ₹{b.totalFare}</p>
                              <p style={{ fontSize: "0.85rem" }}>
                                Status: <span style={{ color: b.bookingStatus === "CANCELLED" ? "#ef4444" : "#10b981", fontWeight: 700 }}>
                                  {b.bookingStatus}
                                </span>
                              </p>
                            </div>
                            {b.bookingStatus !== "CANCELLED" && (
                              <button className="cancel-booking-btn" onClick={() => handleCancelTrain(b.id)}>
                                Cancel Ticket
                              </button>
                            )}
                          </div>
                        </div>
                      ))
                    }
                  </div>
                )}
              </div>
            )}

            {/* OVERVIEW PANEL */}
            {openPanel === "overview" && (
              <div className="glass-card" style={{ padding: 30 }}>
                <h2>LuxNes Traveler Account</h2>
                <p style={{ color: "#64748b", margin: "10px 0 30px 0" }}>Manage your account settings, view active tickets and interact with customer support portals.</p>
                
                <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: 20 }}>
                  <div style={{ background: "#ecfdf5", border: "1px solid #a7f3d0", padding: 20, borderRadius: 16 }}>
                    <h3 style={{ color: "#065f46" }}>{bookings.length}</h3>
                    <p style={{ color: "#065f46" }}>Hotel Stays</p>
                  </div>
                  <div style={{ background: "#eff6ff", border: "1px solid #bfdbfe", padding: 20, borderRadius: 16 }}>
                    <h3 style={{ color: "#1e40af" }}>{flightBookings.filter(b => b.bookingStatus !== "CANCELLED").length}</h3>
                    <p style={{ color: "#1e40af" }}>Active Flights</p>
                  </div>
                  <div style={{ background: "#fff7ed", border: "1px solid #fed7aa", padding: 20, borderRadius: 16 }}>
                    <h3 style={{ color: "#c2410c" }}>{busBookings.filter(b => b.bookingStatus !== "CANCELLED").length}</h3>
                    <p style={{ color: "#c2410c" }}>Active Buses</p>
                  </div>
                  <div style={{ background: "#f5f3ff", border: "1px solid #ddd6fe", padding: 20, borderRadius: 16 }}>
                    <h3 style={{ color: "#5b21b6" }}>{trainBookings.filter(b => b.bookingStatus !== "CANCELLED").length}</h3>
                    <p style={{ color: "#5b21b6" }}>Active Trains</p>
                  </div>
                </div>
              </div>
            )}

            {/* TRANSACTIONS PANEL */}
            {openPanel === "transactions" && (
              <div className="glass-card">
                <h2>Transaction History</h2>
                <p style={{ color: "#64748b", marginBottom: 20 }}>Unified billing logs for all payments and reservations.</p>

                <div style={{ overflowX: "auto" }}>
                  <table style={{ width: "100%", borderCollapse: "collapse", textAlign: "left" }}>
                    <thead>
                      <tr style={{ borderBottom: "2px solid #e2e8f0" }}>
                        <th style={{ padding: "12px 16px", color: "#475569", fontWeight: 600 }}>Txn ID</th>
                        <th style={{ padding: "12px 16px", color: "#475569", fontWeight: 600 }}>Type</th>
                        <th style={{ padding: "12px 16px", color: "#475569", fontWeight: 600 }}>Details</th>
                        <th style={{ padding: "12px 16px", color: "#475569", fontWeight: 600 }}>Date</th>
                        <th style={{ padding: "12px 16px", color: "#475569", fontWeight: 600 }}>Amount</th>
                        <th style={{ padding: "12px 16px", color: "#475569", fontWeight: 600 }}>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {getAllTransactions().length === 0 ? (
                        <tr>
                          <td colSpan="6" style={{ padding: 20, textAlign: "center", color: "#64748b" }}>No transaction records found.</td>
                        </tr>
                      ) : (
                        getAllTransactions().map((tx, idx) => (
                          <tr key={idx} style={{ borderBottom: "1px solid #f1f5f9" }}>
                            <td style={{ padding: "12px 16px", fontSize: "0.9rem", fontFamily: "monospace", color: "#64748b" }}>{tx.id}</td>
                            <td style={{ padding: "12px 16px" }}>
                              <span style={{
                                backgroundColor: "#f1f5f9",
                                padding: "4px 8px",
                                borderRadius: 6,
                                fontSize: "0.8rem",
                                fontWeight: 600,
                                color: "#475569"
                              }}>
                                {tx.type}
                              </span>
                            </td>
                            <td style={{ padding: "12px 16px", fontSize: "0.9rem", fontWeight: 500 }}>{tx.details}</td>
                            <td style={{ padding: "12px 16px", fontSize: "0.9rem" }}>{tx.date}</td>
                            <td style={{ padding: "12px 16px", fontWeight: 700, color: "#0f172a" }}>₹{tx.amount}</td>
                            <td style={{ padding: "12px 16px" }}>
                              <span style={{
                                backgroundColor: tx.status === "CANCELLED" ? "#fef2f2" : "#ecfdf5",
                                color: tx.status === "CANCELLED" ? "#ef4444" : "#10b981",
                                padding: "4px 8px",
                                borderRadius: 6,
                                fontSize: "0.8rem",
                                fontWeight: 600
                              }}>
                                {tx.status}
                              </span>
                            </td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        </div>
      </main>

      {openAI && <AIChatModal onClose={() => setOpenAI(false)} />}
    </div>
  );
}

export default Dashboard;