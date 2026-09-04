import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import {
  User,
  CreditCard,
  Hotel as HotelIcon,
  Plane,
  Bus,
  Train,
  Settings,
  History,
  HelpCircle,
  Heart,
  LogOut,
  Menu,
  X,
  Eye,
  EyeOff,
  Search,
  MapPin,
  ExternalLink,
  Receipt,
  Calendar,
  ArrowRightLeft
} from "lucide-react";
import { startBookingPayment } from "../payment/RazorpayPayment";
import { getUserFlightBookings, cancelFlightBooking } from "../api/flightBookingApi";
import { getUserBusBookings, cancelBusBooking } from "../api/busBookingApi";
import { getUserTrainBookings, cancelTrainBooking } from "../api/trainBookingApi";
import { getInitialTheme, applyTheme, THEME_CONFIG } from "../utils/theme";
import HomeNavbar from "../components/HomeNavbar";
import Footer from "../components/Footer";
import "../styles/Dashboard.css";

const API_BASE = process.env.REACT_APP_API_URL || "http://localhost:8080";

function Dashboard() {
  const navigate = useNavigate();

  // Navigation & Menu State (8 requested menus)
  // "overview", "profile", "search-book", "bookings", "transactions", "favorites", "support", "settings"
  const [activeMenu, setActiveMenu] = useState("overview");
  const [mobileDrawerOpen, setMobileDrawerOpen] = useState(false);

  // User Profile State
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isEditingProfile, setIsEditingProfile] = useState(false);
  const [profileForm, setProfileForm] = useState({ name: "", mobile: "", city: "" });
  const [savingProfile, setSavingProfile] = useState(false);

  // Bookings Data State (Hotels, Flights, Buses, Trains)
  const [hotelBookings, setHotelBookings] = useState([]);
  const [flightBookings, setFlightBookings] = useState([]);
  const [busBookings, setBusBookings] = useState([]);
  const [trainBookings, setTrainBookings] = useState([]);
  const [bookingsFilter, setBookingsFilter] = useState("all"); // all, hotels, flights, buses, trains

  // Search & Book Category State ("hotels", "flights", "trains", "buses")
  const [searchCategory, setSearchCategory] = useState("hotels");
  const [hotelQuery, setHotelQuery] = useState("");
  const [hotelResults, setHotelResults] = useState([]);
  const [searchingHotels, setSearchingHotels] = useState(false);

  const [transportSource, setTransportSource] = useState("");
  const [transportDest, setTransportDest] = useState("");
  const [transportDate, setTransportDate] = useState(new Date().toISOString().split("T")[0]);
  const [transportQuery, setTransportQuery] = useState("");
  const [flightResults, setFlightResults] = useState([]);
  const [trainResults, setTrainResults] = useState([]);
  const [busResults, setBusResults] = useState([]);
  const [searchingTransport, setSearchingTransport] = useState(false);

  // Booking Modals State
  const [activeModal, setActiveModal] = useState(null); // { type: 'hotel'|'flight'|'train'|'bus', item: {...} }
  const [bookingFormData, setBookingFormData] = useState({
    passengerName: "",
    passengerAge: 28,
    passengerGender: "Male",
    numberOfSeats: 1,
    journeyDate: new Date().toISOString().split("T")[0]
  });
  const [submittingBooking, setSubmittingBooking] = useState(false);

  // Receipt Modal State
  const [selectedReceipt, setSelectedReceipt] = useState(null);

  // Transactions Ledger & Search / Filter State
  const [dbTransactions, setDbTransactions] = useState([]);
  const [txnSearch, setTxnSearch] = useState("");
  const [txnTypeFilter, setTxnTypeFilter] = useState("ALL");
  const [txnStatusFilter, setTxnStatusFilter] = useState("ALL");

  // Favorites State (Stored per user in localStorage)
  const [favorites, setFavorites] = useState([]);

  // Support / Complaints State
  const [complaints, setComplaints] = useState([]);
  const [newComplaint, setNewComplaint] = useState({ message: "" });
  const [submittingComplaint, setSubmittingComplaint] = useState(false);

  // Settings State
  const [passwordForm, setPasswordForm] = useState({ currentPassword: "", newPassword: "", confirmPassword: "" });
  const [showPassword, setShowPassword] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);
  const [currentTheme, setCurrentTheme] = useState(getInitialTheme());
  const [disableModalOpen, setDisableModalOpen] = useState(false);
  const [disablePassword, setDisablePassword] = useState("");
  const [disablingAccount, setDisablingAccount] = useState(false);

  /* ------------------- 1. INITIAL LOAD ------------------- */
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      navigate("/login");
      return;
    }

    const fetchInitialData = async () => {
      try {
        // 1. Fetch user profile
        const res = await axios.get(`${API_BASE}/api/dashboard/profile`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        const profileData = res.data;
        setUser(profileData);
        setProfileForm({
          name: profileData.name || "",
          mobile: profileData.mobile || "",
          city: profileData.city || ""
        });
        setBookingFormData((prev) => ({
          ...prev,
          passengerName: profileData.name || ""
        }));

        // 2. Load user favorites from localStorage
        const email = profileData.email || localStorage.getItem("email");
        if (email) {
          const storedFavs = localStorage.getItem(`luxnes_favs_${email}`);
          if (storedFavs) {
            try { setFavorites(JSON.parse(storedFavs)); } catch (e) {}
          }
        }

        // 3. Load all real bookings and transactions ledger
        if (profileData && profileData.id) {
          await loadAllBookings(profileData.id, profileData.email || localStorage.getItem("email"), token);
          await loadComplaints(profileData.id, token);
          await loadTransactions(token);
        } else {
          await loadTransactions(token);
        }

        // 4. Preload active hotels for search
        fetchInitialHotels(token);
      } catch (err) {
        console.error("Profile load failed:", err);
        navigate("/login");
      } finally {
        setLoading(false);
      }
    };

    fetchInitialData();
  }, [navigate]);

  /* ------------------- 2. DATA LOADERS ------------------- */
  const loadAllBookings = async (userId, userEmail, token) => {
    try {
      // Hotels
      if (userEmail) {
        const hotelRes = await axios.get(`${API_BASE}/api/bookings/getuserbookings?email=${userEmail}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setHotelBookings(hotelRes.data?.data || hotelRes.data || []);
      }

      // Flights
      const flightRes = await getUserFlightBookings(userId);
      setFlightBookings(flightRes.data?.data || []);

      // Buses
      const busRes = await getUserBusBookings(userId);
      setBusBookings(busRes.data?.data || []);

      // Trains
      const trainRes = await getUserTrainBookings(userId);
      setTrainBookings(trainRes.data?.data || []);
    } catch (err) {
      console.error("Failed to load user bookings:", err);
    }
  };

  const loadComplaints = async (userId, token) => {
    try {
      const res = await axios.get(`${API_BASE}/api/dashboard/user/${userId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setComplaints(res.data || []);
    } catch (err) {
      console.error("Failed to load complaints:", err);
    }
  };

  const loadTransactions = async (token) => {
    try {
      const res = await axios.get(`${API_BASE}/api/payment/my-transactions`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.data && Array.isArray(res.data)) {
        setDbTransactions(res.data);
      }
    } catch (err) {
      console.warn("Transactions ledger loaded from local bookings fallback:", err.message);
    }
  };

  const fetchInitialHotels = async (token) => {
    try {
      const res = await axios.get(`${API_BASE}/api/dashboard/get-active-hotel`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.data && Array.isArray(res.data)) {
        setHotelResults(res.data);
      } else if (res.data?.data && Array.isArray(res.data.data)) {
        setHotelResults(res.data.data);
      }
    } catch (err) {
      console.error("Failed to fetch initial hotels:", err);
    }
  };

  /* ------------------- 3. FAVORITES MANAGEMENT ------------------- */
  const toggleFavorite = (hotel) => {
    const email = user?.email || localStorage.getItem("email");
    if (!email) return;

    let updated;
    const exists = favorites.some((f) => f.id === hotel.id);
    if (exists) {
      updated = favorites.filter((f) => f.id !== hotel.id);
    } else {
      updated = [...favorites, hotel];
    }
    setFavorites(updated);
    localStorage.setItem(`luxnes_favs_${email}`, JSON.stringify(updated));
  };

  const isFavorited = (hotelId) => favorites.some((f) => f.id === hotelId);

  /* ------------------- 4. SEARCH ACTIONS ------------------- */
  const handleSearchHotels = async () => {
    setSearchingHotels(true);
    try {
      const token = localStorage.getItem("token");
      const url = hotelQuery.trim()
        ? `${API_BASE}/api/dashboard/search-hotels?query=${encodeURIComponent(hotelQuery.trim())}`
        : `${API_BASE}/api/dashboard/get-active-hotel`;
      const res = await axios.get(url, { headers: { Authorization: `Bearer ${token}` } });
      const data = res.data?.data || res.data || [];
      setHotelResults(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("Hotel search error:", err);
      alert("Failed to search hotels.");
    } finally {
      setSearchingHotels(false);
    }
  };

  const handleSearchTransport = async (overrideSrc, overrideDst) => {
    setSearchingTransport(true);
    try {
      const token = localStorage.getItem("token");
      const config = { headers: { Authorization: `Bearer ${token}` } };
      const s = (overrideSrc !== undefined ? overrideSrc : transportSource).trim();
      const d = (overrideDst !== undefined ? overrideDst : transportDest).trim();
      const dt = transportDate;
      const q = transportQuery.trim();

      const params = new URLSearchParams();
      if (s) params.append("source", s);
      if (d) params.append("destination", d);
      if (dt) params.append("date", dt);
      if (q) params.append("query", q);

      const qs = params.toString();

      if (searchCategory === "flights") {
        const url = qs ? `${API_BASE}/api/flights/search?${qs}` : `${API_BASE}/api/flights`;
        const res = await axios.get(url, config);
        setFlightResults(res.data?.data || res.data || []);
      } else if (searchCategory === "trains") {
        const url = qs ? `${API_BASE}/api/trains/search?${qs}` : `${API_BASE}/api/trains`;
        const res = await axios.get(url, config);
        setTrainResults(res.data?.data || res.data || []);
      } else if (searchCategory === "buses") {
        const url = qs ? `${API_BASE}/api/bus/search?${qs}` : `${API_BASE}/api/bus`;
        const res = await axios.get(url, config);
        setBusResults(res.data?.data || res.data || []);
      }
    } catch (err) {
      console.error("Transport search error:", err);
    } finally {
      setSearchingTransport(false);
    }
  };

  // Switch category and prefetch default catalog if empty
  const handleCategorySwitch = (cat) => {
    setSearchCategory(cat);
    const token = localStorage.getItem("token");
    const config = { headers: { Authorization: `Bearer ${token}` } };

    if (cat === "flights" && flightResults.length === 0) {
      axios.get(`${API_BASE}/api/flights`, config).then((r) => setFlightResults(r.data?.data || r.data || [])).catch(() => {});
    } else if (cat === "trains" && trainResults.length === 0) {
      axios.get(`${API_BASE}/api/trains`, config).then((r) => setTrainResults(r.data?.data || r.data || [])).catch(() => {});
    } else if (cat === "buses" && busResults.length === 0) {
      axios.get(`${API_BASE}/api/bus`, config).then((r) => setBusResults(r.data?.data || r.data || [])).catch(() => {});
    }
  };

  /* ------------------- 5. BOOKING SUBMISSIONS ------------------- */
  const openBookingModal = (type, item) => {
    setBookingFormData((prev) => ({
      ...prev,
      journeyDate: transportDate || new Date().toISOString().split("T")[0]
    }));
    setActiveModal({ type, item });
  };

  const handleConfirmBooking = async () => {
    if (!activeModal) return;
    const { type, item } = activeModal;
    const token = localStorage.getItem("token");
    setSubmittingBooking(true);

    try {
      if (type === "hotel") {
        await axios.post(`${API_BASE}/api/bookings/bookhotel`, { hotelId: item.id }, {
          headers: { Authorization: `Bearer ${token}` }
        });
        alert("✅ Hotel booking reserved successfully!");
      } else if (type === "flight") {
        await axios.post(`${API_BASE}/api/flight-bookings/book`, {
          userId: user.id,
          flightId: item.id,
          passengerName: bookingFormData.passengerName,
          passengerAge: Number(bookingFormData.passengerAge),
          passengerGender: bookingFormData.passengerGender,
          numberOfSeats: Number(bookingFormData.numberOfSeats),
          journeyDate: bookingFormData.journeyDate
        }, { headers: { Authorization: `Bearer ${token}` } });
        alert("✅ Flight ticket booked successfully!");
      } else if (type === "train") {
        await axios.post(`${API_BASE}/api/train-bookings/book`, {
          userId: user.id,
          trainId: item.id,
          passengerName: bookingFormData.passengerName,
          passengerAge: Number(bookingFormData.passengerAge),
          passengerGender: bookingFormData.passengerGender,
          numberOfSeats: Number(bookingFormData.numberOfSeats),
          journeyDate: bookingFormData.journeyDate
        }, { headers: { Authorization: `Bearer ${token}` } });
        alert("✅ Train ticket booked successfully!");
      } else if (type === "bus") {
        await axios.post(`${API_BASE}/api/bus-bookings/book`, {
          userId: user.id,
          busId: item.id,
          passengerName: bookingFormData.passengerName,
          passengerAge: Number(bookingFormData.passengerAge),
          passengerGender: bookingFormData.passengerGender,
          numberOfSeats: Number(bookingFormData.numberOfSeats),
          journeyDate: bookingFormData.journeyDate
        }, { headers: { Authorization: `Bearer ${token}` } });
        alert("✅ Bus seat booked successfully!");
      }

      // Refresh bookings
      setActiveModal(null);
      await loadAllBookings(user.id, user.email, token);
      setActiveMenu("bookings");
    } catch (err) {
      console.error("Booking error:", err);
      const msg = err.response?.data?.error?.message || err.response?.data?.message || "Booking failed.";
      alert(`⚠️ ${msg}`);
    } finally {
      setSubmittingBooking(false);
    }
  };

  /* ------------------- 6. CANCELLATION FLOWS ------------------- */
  const handleCancelHotel = async (id) => {
    if (!window.confirm("Are you sure you want to cancel this hotel booking?")) return;
    try {
      const token = localStorage.getItem("token");
      await axios.put(`${API_BASE}/api/bookings/cancel/${id}`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });
      alert("✅ Hotel booking cancelled.");
      loadAllBookings(user.id, user.email, token);
    } catch (err) {
      alert("Failed to cancel hotel booking.");
    }
  };

  const handleCancelFlight = async (id) => {
    if (!window.confirm("Are you sure you want to cancel this flight ticket?")) return;
    try {
      await cancelFlightBooking(id);
      alert("✅ Flight booking cancelled.");
      loadAllBookings(user.id, user.email, localStorage.getItem("token"));
    } catch (err) {
      alert("Failed to cancel flight booking.");
    }
  };

  const handleCancelTrain = async (id) => {
    if (!window.confirm("Are you sure you want to cancel this train ticket?")) return;
    try {
      await cancelTrainBooking(id);
      alert("✅ Train ticket cancelled.");
      loadAllBookings(user.id, user.email, localStorage.getItem("token"));
    } catch (err) {
      alert("Failed to cancel train booking.");
    }
  };

  const handleCancelBus = async (id) => {
    if (!window.confirm("Are you sure you want to cancel this bus seat?")) return;
    try {
      await cancelBusBooking(id);
      alert("✅ Bus booking cancelled.");
      loadAllBookings(user.id, user.email, localStorage.getItem("token"));
    } catch (err) {
      alert("Failed to cancel bus booking.");
    }
  };

  /* ------------------- 7. PROFILE & SETTINGS ------------------- */
  const handleSaveProfile = async () => {
    setSavingProfile(true);
    try {
      const token = localStorage.getItem("token");
      const res = await axios.put(`${API_BASE}/api/dashboard/update-profile`, profileForm, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setUser(res.data);
      setIsEditingProfile(false);
      alert("✅ Profile updated successfully!");
    } catch (err) {
      alert("Failed to update profile.");
    } finally {
      setSavingProfile(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      alert("New password and confirm password do not match!");
      return;
    }
    if (passwordForm.newPassword.length < 6) {
      alert("New password must be at least 6 characters!");
      return;
    }
    setSavingPassword(true);
    try {
      const token = localStorage.getItem("token");
      await axios.put(`${API_BASE}/api/dashboard/change-password`, passwordForm, {
        headers: { Authorization: `Bearer ${token}` }
      });
      alert("✅ Password changed successfully! Please keep it secure.");
      setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
    } catch (err) {
      alert(err.response?.data?.message || "Failed to change password.");
    } finally {
      setSavingPassword(false);
    }
  };

  const handleThemeSelect = (themeId) => {
    const updated = applyTheme(themeId);
    setCurrentTheme(updated);
  };

  const handleDisableAccount = async () => {
    if (!disablePassword) {
      alert("Please enter your current password to confirm account deactivation.");
      return;
    }
    setDisablingAccount(true);
    try {
      const token = localStorage.getItem("token");
      await axios.patch(`${API_BASE}/api/dashboard/disable-account`, {
        email: user.email,
        password: disablePassword
      }, { headers: { Authorization: `Bearer ${token}` } });
      alert("Your account has been deactivated.");
      localStorage.clear();
      navigate("/login");
    } catch (err) {
      alert(err.response?.data?.message || "Deactivation failed. Check password.");
    } finally {
      setDisablingAccount(false);
    }
  };

  /* ------------------- 8. SUPPORT & COMPLAINTS ------------------- */
  const handleSubmitComplaint = async (e) => {
    e.preventDefault();
    if (!newComplaint.message.trim()) {
      alert("Please enter your support query or message.");
      return;
    }
    setSubmittingComplaint(true);
    try {
      const token = localStorage.getItem("token");
      await axios.post(`${API_BASE}/api/dashboard/add`, {
        userId: user.id,
        bookingId: null,
        message: newComplaint.message.trim()
      }, { headers: { Authorization: `Bearer ${token}` } });
      alert("✅ Support ticket submitted successfully! NextGem support will review shortly.");
      setNewComplaint({ message: "" });
      loadComplaints(user.id, token);
    } catch (err) {
      alert("Failed to submit support ticket.");
    } finally {
      setSubmittingComplaint(false);
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate("/login");
  };

  /* ------------------- 9. TRANSACTIONS LEDGER ------------------- */
  const getAllTransactions = () => {
    if (dbTransactions && dbTransactions.length > 0) {
      return dbTransactions.map((p) => ({
        id: p.razorpayPaymentId || p.razorpayOrderId || `TXN-PAY-${p.id}`,
        orderId: p.razorpayOrderId,
        paymentId: p.razorpayPaymentId,
        type: p.bookingType ? `${p.bookingType.toUpperCase()} Booking` : "Travel Booking",
        bookingType: p.bookingType ? p.bookingType.toUpperCase() : "GENERAL",
        details: p.description || `${p.bookingType} Booking #${p.bookingId}`,
        date: p.createdAt ? new Date(p.createdAt).toLocaleDateString("en-IN", { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }) : "N/A",
        amount: p.amount || 0,
        status: p.status === "PAID" ? "PAID" : (p.status || "CONFIRMED"),
        method: p.paymentMethod || "UPI / ONLINE",
        raw: p
      }));
    }

    const list = [];

    hotelBookings.forEach((b) => {
      list.push({
        id: `TXN-HTL-${b.id || "101"}`,
        type: "Hotel Stay",
        bookingType: "HOTEL",
        details: b.hotel?.hotel || b.hotelName || "Hotel Stay Reservation",
        date: b.checkIn ? b.checkIn.split("T")[0] : "N/A",
        amount: b.amount || 3200,
        status: b.bookingStatus === "CANCELLED" ? "CANCELLED" : "CONFIRMED",
        method: "UPI",
        raw: b
      });
    });

    flightBookings.forEach((b) => {
      list.push({
        id: `TXN-FLT-${b.id || "201"}`,
        type: "Flight Ticket",
        bookingType: "FLIGHT",
        details: `Flight ID #${b.flightId} | Passenger: ${b.passengerName}`,
        date: b.journeyDate || "N/A",
        amount: b.totalFare || 0,
        status: b.bookingStatus || "CONFIRMED",
        method: "UPI",
        raw: b
      });
    });

    trainBookings.forEach((b) => {
      list.push({
        id: `TXN-TRN-${b.id || "301"}`,
        type: "Train Ticket",
        bookingType: "TRAIN",
        details: `Train ID #${b.trainId} | Passenger: ${b.passengerName}`,
        date: b.journeyDate || "N/A",
        amount: b.totalFare || 0,
        status: b.bookingStatus || "CONFIRMED",
        method: "UPI",
        raw: b
      });
    });

    busBookings.forEach((b) => {
      list.push({
        id: `TXN-BUS-${b.id || "401"}`,
        type: "Bus Seat",
        bookingType: "BUS",
        details: `Bus ID #${b.busId} | Passenger: ${b.passengerName}`,
        date: b.journeyDate || "N/A",
        amount: b.totalFare || 0,
        status: b.bookingStatus || "CONFIRMED",
        method: "UPI",
        raw: b
      });
    });

    return list;
  };

  const transactions = getAllTransactions();

  const filteredTransactions = transactions.filter((tx) => {
    const q = txnSearch.trim().toLowerCase();
    const matchesSearch =
      !q ||
      tx.id.toLowerCase().includes(q) ||
      (tx.orderId && tx.orderId.toLowerCase().includes(q)) ||
      (tx.details && tx.details.toLowerCase().includes(q)) ||
      (tx.type && tx.type.toLowerCase().includes(q));

    const matchesType =
      txnTypeFilter === "ALL" ||
      (tx.bookingType && tx.bookingType.toUpperCase() === txnTypeFilter) ||
      (tx.type && tx.type.toUpperCase().includes(txnTypeFilter));

    const matchesStatus =
      txnStatusFilter === "ALL" ||
      tx.status.toUpperCase() === txnStatusFilter;

    return matchesSearch && matchesType && matchesStatus;
  });

  const totalSpend = transactions.reduce((acc, t) => (t.status !== "CANCELLED" && t.status !== "FAILED") ? acc + (Number(t.amount) || 0) : acc, 0);

  if (loading) {
    return (
      <div className="user-dashboard-page" style={{ justifyContent: "center", alignItems: "center" }}>
        <div style={{ textAlign: "center" }}>
          <div className="auth-brand-badge" style={{ marginBottom: "16px" }}>
            <img src="/assets/logo-badge.png" alt="Loading" className="auth-logo-img" />
          </div>
          <h3>Loading Traveler Dashboard...</h3>
        </div>
      </div>
    );
  }

  return (
    <div className="user-dashboard-page">
      {/* 1. Global Navbar with Theme Toggle */}
      <HomeNavbar />

      {/* Mobile Drawer Bar */}
      <div className="mobile-sidebar-toggle-bar">
        <span style={{ fontWeight: 700, fontSize: "0.95rem" }}>
          Dashboard: <strong style={{ color: "var(--accent-cyan, #38bdf8)" }}>{activeMenu.toUpperCase()}</strong>
        </span>
        <button
          className="mobile-sidebar-toggle-btn"
          onClick={() => setMobileDrawerOpen(!mobileDrawerOpen)}
        >
          {mobileDrawerOpen ? <X size={16} /> : <Menu size={16} />}
          <span>{mobileDrawerOpen ? "Close Menu" : "Menu"}</span>
        </button>
      </div>

      {/* 2. Main Dashboard Shell (Sidebar + Content) */}
      <div className="user-dashboard-shell">
        {/* SIDEBAR NAVIGATION (8 Dedicated Menus) */}
        <aside className={`user-dashboard-sidebar ${mobileDrawerOpen ? "mobile-open" : ""}`}>
          <div className="sidebar-user-summary">
            <img
              src="/assets/logo-badge.png"
              alt="Avatar"
              className="sidebar-avatar"
            />
            <div className="sidebar-user-details">
              <span className="sidebar-user-name">{user?.name || "Traveler"}</span>
              <span className="sidebar-user-role">{user?.email || "Traveler Member"}</span>
            </div>
          </div>

          <nav>
            <ul className="user-sidebar-menu">
              <li
                className={`user-sidebar-item ${activeMenu === "overview" ? "active" : ""}`}
                onClick={() => { setActiveMenu("overview"); setMobileDrawerOpen(false); }}
              >
                <CreditCard size={18} />
                <span>Overview</span>
              </li>

              <li
                className={`user-sidebar-item ${activeMenu === "profile" ? "active" : ""}`}
                onClick={() => { setActiveMenu("profile"); setMobileDrawerOpen(false); }}
              >
                <User size={18} />
                <span>Profile</span>
              </li>

              <li
                className={`user-sidebar-item ${activeMenu === "search-book" ? "active" : ""}`}
                onClick={() => { setActiveMenu("search-book"); setMobileDrawerOpen(false); }}
              >
                <Search size={18} />
                <span>Search & Book</span>
              </li>

              <li
                className={`user-sidebar-item ${activeMenu === "bookings" ? "active" : ""}`}
                onClick={() => { setActiveMenu("bookings"); setMobileDrawerOpen(false); }}
              >
                <HotelIcon size={18} />
                <span>My Bookings</span>
                <span className="user-sidebar-item-badge">
                  {hotelBookings.length + flightBookings.length + trainBookings.length + busBookings.length}
                </span>
              </li>

              <li
                className={`user-sidebar-item ${activeMenu === "transactions" ? "active" : ""}`}
                onClick={() => { setActiveMenu("transactions"); setMobileDrawerOpen(false); }}
              >
                <History size={18} />
                <span>Transactions</span>
              </li>

              <li
                className={`user-sidebar-item ${activeMenu === "favorites" ? "active" : ""}`}
                onClick={() => { setActiveMenu("favorites"); setMobileDrawerOpen(false); }}
              >
                <Heart size={18} />
                <span>Favorites</span>
                {favorites.length > 0 && <span className="user-sidebar-item-badge">{favorites.length}</span>}
              </li>

              <li
                className={`user-sidebar-item ${activeMenu === "support" ? "active" : ""}`}
                onClick={() => { setActiveMenu("support"); setMobileDrawerOpen(false); }}
              >
                <HelpCircle size={18} />
                <span>Support</span>
              </li>

              <li
                className={`user-sidebar-item ${activeMenu === "settings" ? "active" : ""}`}
                onClick={() => { setActiveMenu("settings"); setMobileDrawerOpen(false); }}
              >
                <Settings size={18} />
                <span>Settings</span>
              </li>
            </ul>
          </nav>

          <div className="user-sidebar-footer">
            <button className="sidebar-ai-btn" onClick={() => window.dispatchEvent(new CustomEvent("open-ai-chat"))}>
              <img src="/assets/ai-agent-logo.png" alt="AI Agent" style={{ width: 20, height: 20, borderRadius: "50%", marginRight: 6, objectFit: "cover" }} />
              <span>AI Trip Agent</span>
            </button>
            <button className="sidebar-logout-btn" onClick={handleLogout}>
              <LogOut size={16} />
              <span>Log Out</span>
            </button>
          </div>
        </aside>

        {/* MAIN PANEL CONTENT */}
        <main className="user-dashboard-main">

          {/* ======================= MENU 1: OVERVIEW ======================= */}
          {activeMenu === "overview" && (
            <div className="dash-card">
              <div className="overview-welcome-banner">
                <div>
                  <h2 className="overview-banner-title">Welcome back, {user?.name || "Traveler"}! 👋</h2>
                  <p className="overview-banner-subtitle">
                    Explore curated hotels, book flights, buses and trains, or plan customized itineraries.
                  </p>
                </div>
                <button
                  className="dash-search-btn"
                  onClick={() => { setActiveMenu("search-book"); setSearchCategory("hotels"); }}
                >
                  Book New Stay ➔
                </button>
              </div>

              {/* 4 Metrics from Real Data */}
              <div className="overview-stats-grid">
                <div className="stat-metric-card">
                  <div className="stat-icon-wrapper" style={{ background: "rgba(2, 132, 199, 0.15)", color: "#0284c7" }}>
                    <HotelIcon size={24} />
                  </div>
                  <div>
                    <div className="stat-number">{hotelBookings.length}</div>
                    <div className="stat-label">Hotel Stays</div>
                  </div>
                </div>

                <div className="stat-metric-card">
                  <div className="stat-icon-wrapper" style={{ background: "rgba(99, 102, 241, 0.15)", color: "#6366f1" }}>
                    <Plane size={24} />
                  </div>
                  <div>
                    <div className="stat-number">{flightBookings.filter(f => f.bookingStatus !== "CANCELLED").length}</div>
                    <div className="stat-label">Active Flights</div>
                  </div>
                </div>

                <div className="stat-metric-card">
                  <div className="stat-icon-wrapper" style={{ background: "rgba(234, 88, 12, 0.15)", color: "#ea580c" }}>
                    <Bus size={24} />
                  </div>
                  <div>
                    <div className="stat-number">{busBookings.filter(b => b.bookingStatus !== "CANCELLED").length}</div>
                    <div className="stat-label">Active Buses</div>
                  </div>
                </div>

                <div className="stat-metric-card">
                  <div className="stat-icon-wrapper" style={{ background: "rgba(139, 92, 246, 0.15)", color: "#8b5cf6" }}>
                    <Train size={24} />
                  </div>
                  <div>
                    <div className="stat-number">{trainBookings.filter(t => t.bookingStatus !== "CANCELLED").length}</div>
                    <div className="stat-label">Active Trains</div>
                  </div>
                </div>
              </div>

              {/* Quick Action Shortcuts */}
              <h3 style={{ fontSize: "1.1rem", marginBottom: "14px", color: "var(--text-main, #ffffff)" }}>
                Quick Travel Shortcuts
              </h3>
              <div className="quick-actions-row">
                <button
                  className="quick-action-btn"
                  onClick={() => { setActiveMenu("search-book"); setSearchCategory("hotels"); }}
                >
                  <HotelIcon size={22} color="#0284c7" />
                  <span>Reserve Hotels</span>
                </button>
                <button
                  className="quick-action-btn"
                  onClick={() => { setActiveMenu("search-book"); setSearchCategory("flights"); }}
                >
                  <Plane size={22} color="#6366f1" />
                  <span>Search Flights</span>
                </button>
                <button
                  className="quick-action-btn"
                  onClick={() => { setActiveMenu("search-book"); setSearchCategory("trains"); }}
                >
                  <Train size={22} color="#8b5cf6" />
                  <span>Find Trains</span>
                </button>
                <button
                  className="quick-action-btn"
                  onClick={() => { setActiveMenu("search-book"); setSearchCategory("buses"); }}
                >
                  <Bus size={22} color="#ea580c" />
                  <span>Book Buses</span>
                </button>
                <button className="quick-action-btn" onClick={() => window.dispatchEvent(new CustomEvent("open-ai-chat"))}>
                  <img src="/assets/ai-agent-logo.png" alt="AI Agent" style={{ width: 24, height: 24, borderRadius: "50%", marginBottom: 4, objectFit: "cover" }} />
                  <span>AI Travel Planner</span>
                </button>
              </div>

              {/* Recent Activity Table */}
              <div className="dash-card-header" style={{ marginTop: "10px" }}>
                <h3 className="dash-card-title" style={{ fontSize: "1.15rem" }}>Recent Bookings</h3>
                <button
                  className="category-tab-btn"
                  onClick={() => setActiveMenu("bookings")}
                  style={{ padding: "6px 12px", fontSize: "0.8rem" }}
                >
                  View All ({transactions.length}) ➔
                </button>
              </div>

              <div className="table-responsive-wrapper">
                <table className="dash-data-table">
                  <thead>
                    <tr>
                      <th>Service</th>
                      <th>Details</th>
                      <th>Date</th>
                      <th>Amount</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {transactions.slice(0, 5).map((item, idx) => (
                      <tr key={idx}>
                        <td><strong>{item.type}</strong></td>
                        <td>{item.details}</td>
                        <td>{item.date}</td>
                        <td style={{ fontWeight: 700, color: "var(--accent-cyan, #38bdf8)" }}>₹{item.amount}</td>
                        <td>
                          <span className={`booking-status-tag ${item.status.toLowerCase()}`}>
                            {item.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                    {transactions.length === 0 && (
                      <tr>
                        <td colSpan={5} style={{ textAlign: "center", padding: "24px", color: "var(--text-muted, #94a3b8)" }}>
                          No recent bookings yet. Start your journey under "Search & Book"!
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* ======================= MENU 2: PROFILE ======================= */}
          {activeMenu === "profile" && (
            <div className="dash-card">
              <div className="dash-card-header">
                <div>
                  <h2 className="dash-card-title">Traveler Profile</h2>
                  <p className="dash-card-subtitle">Manage personal contact details and member credentials</p>
                </div>
                {!isEditingProfile ? (
                  <button className="dash-search-btn" onClick={() => setIsEditingProfile(true)}>
                    Edit Profile Details
                  </button>
                ) : (
                  <div style={{ display: "flex", gap: "10px" }}>
                    <button className="dash-search-btn" onClick={handleSaveProfile} disabled={savingProfile}>
                      {savingProfile ? "Saving..." : "Save Changes"}
                    </button>
                    <button
                      className="category-tab-btn"
                      onClick={() => {
                        setIsEditingProfile(false);
                        setProfileForm({ name: user.name || "", mobile: user.mobile || "", city: user.city || "" });
                      }}
                    >
                      Cancel
                    </button>
                  </div>
                )}
              </div>

              <div className="profile-view-grid">
                <div className="profile-info-block">
                  <div className="profile-info-label">Full Name</div>
                  {isEditingProfile ? (
                    <input
                      type="text"
                      className="auth-input"
                      value={profileForm.name}
                      onChange={(e) => setProfileForm({ ...profileForm, name: e.target.value })}
                    />
                  ) : (
                    <div className="profile-info-value">{user?.name || "Not provided"}</div>
                  )}
                </div>

                <div className="profile-info-block">
                  <div className="profile-info-label">Registered Email</div>
                  <div className="profile-info-value" style={{ color: "var(--text-muted, #94a3b8)" }}>
                    {user?.email}
                  </div>
                  <span style={{ fontSize: "0.75rem", color: "var(--accent-green, #10b981)", fontWeight: 600 }}>
                    Verified Account
                  </span>
                </div>

                <div className="profile-info-block">
                  <div className="profile-info-label">Mobile Number</div>
                  {isEditingProfile ? (
                    <input
                      type="text"
                      className="auth-input"
                      value={profileForm.mobile}
                      onChange={(e) => setProfileForm({ ...profileForm, mobile: e.target.value })}
                    />
                  ) : (
                    <div className="profile-info-value">{user?.mobile || "Not specified"}</div>
                  )}
                </div>

                <div className="profile-info-block">
                  <div className="profile-info-label">City of Residence</div>
                  {isEditingProfile ? (
                    <input
                      type="text"
                      className="auth-input"
                      value={profileForm.city}
                      onChange={(e) => setProfileForm({ ...profileForm, city: e.target.value })}
                    />
                  ) : (
                    <div className="profile-info-value">{user?.city || "Not specified"}</div>
                  )}
                </div>

                <div className="profile-info-block">
                  <div className="profile-info-label">Account Role</div>
                  <div className="profile-info-value" style={{ color: "var(--accent-cyan, #38bdf8)" }}>
                    {user?.role || "ROLE_USER"}
                  </div>
                </div>

                <div className="profile-info-block">
                  <div className="profile-info-label">Account Status</div>
                  <div className="profile-info-value" style={{ color: "var(--accent-green, #10b981)" }}>
                    Active & Verified
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* ======================= MENU 3: SEARCH & BOOK ======================= */}
          {activeMenu === "search-book" && (
            <div className="dash-card">
              <div className="dash-card-header">
                <div>
                  <h2 className="dash-card-title">Search & Book Services</h2>
                  <p className="dash-card-subtitle">
                    Real-time reservation system for verified hotels, domestic flights, trains and buses
                  </p>
                </div>
              </div>

              {/* Subtabs for Hotels, Flights, Trains, Buses */}
              <div className="search-category-tabs">
                <button
                  className={`category-tab-btn ${searchCategory === "hotels" ? "active" : ""}`}
                  onClick={() => handleCategorySwitch("hotels")}
                >
                  <HotelIcon size={16} /> Hotels & Stays
                </button>
                <button
                  className={`category-tab-btn ${searchCategory === "flights" ? "active" : ""}`}
                  onClick={() => handleCategorySwitch("flights")}
                >
                  <Plane size={16} /> Flights
                </button>
                <button
                  className={`category-tab-btn ${searchCategory === "trains" ? "active" : ""}`}
                  onClick={() => handleCategorySwitch("trains")}
                >
                  <Train size={16} /> Trains
                </button>
                <button
                  className={`category-tab-btn ${searchCategory === "buses" ? "active" : ""}`}
                  onClick={() => handleCategorySwitch("buses")}
                >
                  <Bus size={16} /> Buses
                </button>
              </div>

              {/* 1. HOTELS SEARCH BAR */}
              {searchCategory === "hotels" && (
                <>
                  <div className="dash-search-bar">
                    <input
                      type="text"
                      className="dash-search-input"
                      placeholder="Search hotels by city, name, or location landmark..."
                      value={hotelQuery}
                      onChange={(e) => setHotelQuery(e.target.value)}
                      onKeyDown={(e) => { if (e.key === "Enter") handleSearchHotels(); }}
                    />
                    <button className="dash-search-btn" onClick={handleSearchHotels} disabled={searchingHotels}>
                      {searchingHotels ? "Searching..." : "Search Stays"}
                    </button>
                  </div>

                  <div className="dash-items-grid">
                    {hotelResults.map((hotel) => (
                      <div key={hotel.id} className="dash-item-card">
                        <div>
                          <div className="item-card-top">
                            <div>
                              <h4 className="item-title">{hotel.hotel}</h4>
                              <span className="item-badge-pill">
                                <MapPin size={12} /> {hotel.city}
                              </span>
                            </div>
                            <button
                              className="item-favorite-btn"
                              onClick={() => toggleFavorite(hotel)}
                              title={isFavorited(hotel.id) ? "Remove from favorites" : "Add to favorites"}
                            >
                              <Heart size={20} fill={isFavorited(hotel.id) ? "#ef4444" : "none"} />
                            </button>
                          </div>

                          <div className="item-details-list" style={{ marginTop: "12px" }}>
                            {/* Real Location & Distance */}
                            <div className="item-details-row">
                              <strong>📍 Location:</strong>
                              <span>{hotel.location || hotel.address || hotel.city}</span>
                            </div>
                            {hotel.latitude && hotel.longitude ? (
                              <div className="item-details-row" style={{ fontSize: "0.78rem", color: "var(--text-dim, #64748b)" }}>
                                🧭 GPS Coordinates: {hotel.latitude.toFixed(3)}, {hotel.longitude.toFixed(3)}
                              </div>
                            ) : null}
                            <div className="item-details-row">
                              <strong>🏨 Available Rooms:</strong>
                              <span>{hotel.roomavl || hotel.availableRooms || 5}</span>
                            </div>
                            <div className="item-details-row">
                              <strong>✉️ Contact:</strong>
                              <span style={{ fontSize: "0.82rem" }}>{hotel.email}</span>
                            </div>
                          </div>
                        </div>

                        <div className="item-card-actions">
                          <div className="item-price-tag">
                            ₹{hotel.price} <span style={{ fontSize: "0.8rem", fontWeight: 500, color: "var(--text-muted, #94a3b8)" }}>/ night</span>
                          </div>
                          <button
                            className="item-book-btn"
                            onClick={() => openBookingModal("hotel", hotel)}
                          >
                            Book Room
                          </button>
                        </div>
                      </div>
                    ))}
                    {hotelResults.length === 0 && (
                      <p style={{ gridColumn: "1/-1", textAlign: "center", padding: "40px", color: "var(--text-muted, #94a3b8)" }}>
                        No hotels found matching your search. Try another city or keyword.
                      </p>
                    )}
                  </div>
                </>
              )}

              {/* 2. TRANSPORT SEARCH BAR (FLIGHTS, TRAINS, BUSES) */}
              {searchCategory !== "hotels" && (
                <>
                  <div className="dash-search-bar" style={{ display: "flex", flexWrap: "wrap", gap: "10px", alignItems: "center" }}>
                    {/* Origin / From */}
                    <div style={{ flex: "1 1 180px", position: "relative", display: "flex", alignItems: "center" }}>
                      <MapPin size={16} style={{ position: "absolute", left: "12px", color: "var(--accent-cyan, #38bdf8)", pointerEvents: "none" }} />
                      <input
                        type="text"
                        className="dash-search-input"
                        placeholder="From (e.g. Delhi, Mumbai)"
                        value={transportSource}
                        onChange={(e) => setTransportSource(e.target.value)}
                        onKeyDown={(e) => { if (e.key === "Enter") handleSearchTransport(); }}
                        style={{ paddingLeft: "36px", width: "100%" }}
                      />
                    </div>

                    {/* Swap Button */}
                    <button
                      type="button"
                      className="category-tab-btn"
                      onClick={() => {
                        const tmp = transportSource;
                        setTransportSource(transportDest);
                        setTransportDest(tmp);
                      }}
                      title="Swap Origin and Destination"
                      style={{ padding: "10px 12px", borderRadius: "10px", border: "1px solid var(--border-glass, rgba(255,255,255,0.15))" }}
                    >
                      <ArrowRightLeft size={16} />
                    </button>

                    {/* Destination / To */}
                    <div style={{ flex: "1 1 180px", position: "relative", display: "flex", alignItems: "center" }}>
                      <MapPin size={16} style={{ position: "absolute", left: "12px", color: "var(--accent-cyan, #38bdf8)", pointerEvents: "none" }} />
                      <input
                        type="text"
                        className="dash-search-input"
                        placeholder="To (e.g. Varanasi, Goa)"
                        value={transportDest}
                        onChange={(e) => setTransportDest(e.target.value)}
                        onKeyDown={(e) => { if (e.key === "Enter") handleSearchTransport(); }}
                        style={{ paddingLeft: "36px", width: "100%" }}
                      />
                    </div>

                    {/* Date Picker (Journey Date) */}
                    <div style={{ flex: "1 1 160px", position: "relative", display: "flex", alignItems: "center" }}>
                      <Calendar size={16} style={{ position: "absolute", left: "12px", color: "var(--accent-cyan, #38bdf8)", pointerEvents: "none" }} />
                      <input
                        type="date"
                        className="dash-search-input"
                        value={transportDate}
                        min={new Date().toISOString().split("T")[0]}
                        onChange={(e) => setTransportDate(e.target.value)}
                        title="Journey Date"
                        style={{ paddingLeft: "36px", width: "100%", colorScheme: "dark" }}
                      />
                    </div>

                    {/* Optional Keyword Search Box */}
                    <div style={{ flex: "1 1 180px", position: "relative", display: "flex", alignItems: "center" }}>
                      <Search size={16} style={{ position: "absolute", left: "12px", color: "var(--text-muted, #94a3b8)", pointerEvents: "none" }} />
                      <input
                        type="text"
                        className="dash-search-input"
                        placeholder={`Search ${searchCategory} by name/no...`}
                        value={transportQuery}
                        onChange={(e) => setTransportQuery(e.target.value)}
                        onKeyDown={(e) => { if (e.key === "Enter") handleSearchTransport(); }}
                        style={{ paddingLeft: "36px", width: "100%" }}
                      />
                    </div>

                    {/* Search and Reset Buttons */}
                    <button className="dash-search-btn" onClick={() => handleSearchTransport()} disabled={searchingTransport}>
                      {searchingTransport ? "Searching..." : `Search ${searchCategory}`}
                    </button>
                    {(transportSource || transportDest || transportQuery) && (
                      <button
                        type="button"
                        className="category-tab-btn"
                        onClick={() => {
                          setTransportSource("");
                          setTransportDest("");
                          setTransportQuery("");
                          handleSearchTransport("", "");
                        }}
                        style={{ padding: "10px 14px", fontSize: "0.85rem" }}
                      >
                        <X size={15} /> Show All
                      </button>
                    )}
                  </div>

                  {/* Popular Quick Route Chips */}
                  <div style={{ display: "flex", alignItems: "center", gap: "8px", flexWrap: "wrap", margin: "10px 0 18px 0" }}>
                    <span style={{ fontSize: "0.8rem", color: "var(--text-muted, #94a3b8)", fontWeight: 600 }}>Popular Routes:</span>
                    {[
                      { src: "New Delhi", dst: "Varanasi", label: "Delhi ➔ Varanasi" },
                      { src: "New Delhi", dst: "Katra", label: "Delhi ➔ Katra" },
                      { src: "New Delhi", dst: "Mumbai", label: "Delhi ➔ Mumbai" },
                      { src: "Mumbai", dst: "Goa", label: "Mumbai ➔ Goa" },
                      { src: "New Delhi", dst: "Bengaluru", label: "Delhi ➔ Bengaluru" },
                      { src: "Bengaluru", dst: "Goa", label: "Bengaluru ➔ Goa" }
                    ].map((rt, idx) => (
                      <button
                        key={idx}
                        type="button"
                        onClick={() => {
                          setTransportSource(rt.src);
                          setTransportDest(rt.dst);
                          handleSearchTransport(rt.src, rt.dst);
                        }}
                        style={{
                          background: "rgba(255, 255, 255, 0.05)",
                          border: "1px solid rgba(255, 255, 255, 0.12)",
                          borderRadius: "999px",
                          color: "var(--accent-cyan, #38bdf8)",
                          padding: "4px 12px",
                          fontSize: "0.78rem",
                          cursor: "pointer",
                          transition: "all 0.2s"
                        }}
                      >
                        {rt.label}
                      </button>
                    ))}
                  </div>

                  {/* FLIGHT RESULTS */}
                  {searchCategory === "flights" && (
                    <div className="dash-items-grid">
                      {flightResults.map((flight) => (
                        <div key={flight.id} className="dash-item-card">
                          <div>
                            <div className="item-card-top">
                              <div>
                                <h4 className="item-title">{flight.airline || "Domestic Express"}</h4>
                                <span className="item-badge-pill">
                                  <Plane size={12} /> Flight #{flight.flightNumber || flight.id}
                                </span>
                              </div>
                              <span style={{ fontSize: "0.75rem", padding: "2px 8px", borderRadius: "999px", background: "rgba(16, 185, 129, 0.15)", color: "#10b981", border: "1px solid rgba(16, 185, 129, 0.3)" }}>
                                ● Active
                              </span>
                            </div>

                            <div className="item-details-list" style={{ marginTop: "12px" }}>
                              <div className="item-details-row">
                                <strong>Route:</strong>
                                <span>{flight.source} ➔ {flight.destination}</span>
                              </div>
                              <div className="item-details-row">
                                <strong>Schedule:</strong>
                                <span>{flight.departureTime || "08:00 AM"} - {flight.arrivalTime || "10:30 AM"}</span>
                              </div>
                              <div className="item-details-row">
                                <strong>Class & Seats:</strong>
                                <span>{flight.flightClass || "Economy"} • {flight.availableSeats || 24} seats available</span>
                              </div>
                              {flight.amenities && (
                                <div className="item-details-row" style={{ fontSize: "0.8rem", color: "var(--text-muted, #94a3b8)" }}>
                                  <strong>Perks:</strong>
                                  <span>{flight.amenities}</span>
                                </div>
                              )}
                            </div>
                          </div>

                          <div className="item-card-actions">
                            <div className="item-price-tag">₹{flight.fare || flight.price || 3500}</div>
                            <button className="item-book-btn" onClick={() => openBookingModal("flight", flight)}>
                              Book Flight
                            </button>
                          </div>
                        </div>
                      ))}
                      {flightResults.length === 0 && (
                        <p style={{ gridColumn: "1/-1", textAlign: "center", padding: "40px", color: "var(--text-muted, #94a3b8)" }}>
                          No flights found for this route or date. Try another city or click a popular route above.
                        </p>
                      )}
                    </div>
                  )}

                  {/* TRAIN RESULTS */}
                  {searchCategory === "trains" && (
                    <div className="dash-items-grid">
                      {trainResults.map((train) => (
                        <div key={train.id} className="dash-item-card">
                          <div>
                            <div className="item-card-top">
                              <div>
                                <h4 className="item-title">{train.trainName || "Superfast Express"}</h4>
                                <span className="item-badge-pill">
                                  <Train size={12} /> Train #{train.trainNumber || train.id}
                                </span>
                              </div>
                              <span style={{ fontSize: "0.75rem", padding: "2px 8px", borderRadius: "999px", background: "rgba(16, 185, 129, 0.15)", color: "#10b981", border: "1px solid rgba(16, 185, 129, 0.3)" }}>
                                ● Real-Time Active
                              </span>
                            </div>

                            <div className="item-details-list" style={{ marginTop: "12px" }}>
                              <div className="item-details-row">
                                <strong>Route:</strong>
                                <span>{train.source} ➔ {train.destination}</span>
                              </div>
                              <div className="item-details-row">
                                <strong>Timing:</strong>
                                <span>{train.departureTime || "06:15 AM"} - {train.arrivalTime || "02:45 PM"}</span>
                              </div>
                              <div className="item-details-row">
                                <strong>Classes & Seats:</strong>
                                <span>{train.trainClass || "CC, EC, 1A, 2A, 3A"} • {train.availableSeats || 50} seats</span>
                              </div>
                              {train.amenities && (
                                <div className="item-details-row" style={{ fontSize: "0.8rem", color: "var(--text-muted, #94a3b8)" }}>
                                  <strong>Amenities:</strong>
                                  <span>{train.amenities}</span>
                                </div>
                              )}
                            </div>
                          </div>

                          <div className="item-card-actions">
                            <div className="item-price-tag">₹{train.fare || train.price || 1450}</div>
                            <button className="item-book-btn" onClick={() => openBookingModal("train", train)}>
                              Book Ticket
                            </button>
                          </div>
                        </div>
                      ))}
                      {trainResults.length === 0 && (
                        <p style={{ gridColumn: "1/-1", textAlign: "center", padding: "40px", color: "var(--text-muted, #94a3b8)" }}>
                          No trains found for this route or date. Try another station or click a popular route above.
                        </p>
                      )}
                    </div>
                  )}

                  {/* BUS RESULTS */}
                  {searchCategory === "buses" && (
                    <div className="dash-items-grid">
                      {busResults.map((bus) => (
                        <div key={bus.id} className="dash-item-card">
                          <div>
                            <div className="item-card-top">
                              <div>
                                <h4 className="item-title">{bus.operatorName || bus.busName || "Luxury Volvo Bus"}</h4>
                                <span className="item-badge-pill">
                                  <Bus size={12} /> Bus #{bus.busNumber || bus.id}
                                </span>
                              </div>
                              <span style={{ fontSize: "0.75rem", padding: "2px 8px", borderRadius: "999px", background: "rgba(16, 185, 129, 0.15)", color: "#10b981", border: "1px solid rgba(16, 185, 129, 0.3)" }}>
                                ● Active
                              </span>
                            </div>

                            <div className="item-details-list" style={{ marginTop: "12px" }}>
                              <div className="item-details-row">
                                <strong>Route:</strong>
                                <span>{bus.source} ➔ {bus.destination}</span>
                              </div>
                              <div className="item-details-row">
                                <strong>Departure:</strong>
                                <span>{bus.departureTime || "09:30 PM"}</span>
                              </div>
                              <div className="item-details-row">
                                <strong>Type & Seats:</strong>
                                <span>{bus.busType || "AC Sleeper"} • {bus.availableSeats || 30} seats available</span>
                              </div>
                              {bus.amenities && (
                                <div className="item-details-row" style={{ fontSize: "0.8rem", color: "var(--text-muted, #94a3b8)" }}>
                                  <strong>Features:</strong>
                                  <span>{bus.amenities}</span>
                                </div>
                              )}
                            </div>
                          </div>

                          <div className="item-card-actions">
                            <div className="item-price-tag">₹{bus.fare || bus.price || 850}</div>
                            <button className="item-book-btn" onClick={() => openBookingModal("bus", bus)}>
                              Book Seat
                            </button>
                          </div>
                        </div>
                      ))}
                      {busResults.length === 0 && (
                        <p style={{ gridColumn: "1/-1", textAlign: "center", padding: "40px", color: "var(--text-muted, #94a3b8)" }}>
                          No buses found for this route or date. Try another city or click a popular route above.
                        </p>
                      )}
                    </div>
                  )}
                </>
              )}
            </div>
          )}

          {/* ======================= MENU 4: MY BOOKINGS ======================= */}
          {activeMenu === "bookings" && (
            <div className="dash-card">
              <div className="dash-card-header">
                <div>
                  <h2 className="dash-card-title">My Bookings</h2>
                  <p className="dash-card-subtitle">Manage upcoming itineraries, tickets, and reservations</p>
                </div>

                <div className="bookings-filter-bar">
                  {["all", "hotels", "flights", "trains", "buses"].map((tab) => (
                    <button
                      key={tab}
                      className={`category-tab-btn ${bookingsFilter === tab ? "active" : ""}`}
                      onClick={() => setBookingsFilter(tab)}
                      style={{ textTransform: "capitalize", padding: "6px 14px", fontSize: "0.82rem" }}
                    >
                      {tab}
                    </button>
                  ))}
                </div>
              </div>

              <div className="bookings-scroll-container">
                {/* 1. HOTELS */}
                {(bookingsFilter === "all" || bookingsFilter === "hotels") &&
                  hotelBookings.map((b) => (
                    <div key={`htl-${b.id}`} className="booking-record-card">
                      <div>
                        <div style={{ display: "flex", alignItems: "center", gap: "8px", marginBottom: "4px" }}>
                          <span className="item-badge-pill"><HotelIcon size={12} /> Hotel Stay</span>
                          <span className={`booking-status-tag ${b.bookingStatus === "CANCELLED" ? "cancelled" : "confirmed"}`}>
                            {b.bookingStatus || "CONFIRMED"}
                          </span>
                        </div>
                        <h4 style={{ fontSize: "1.1rem", fontWeight: 700 }}>{b.hotel?.hotel || b.hotelName || "Hotel Stay"}</h4>
                        <p style={{ fontSize: "0.88rem", color: "var(--text-muted, #94a3b8)", marginTop: "4px" }}>
                          📍 City: {b.hotel?.city || "N/A"} | Price: ₹{b.amount || 2500}
                        </p>
                        <p style={{ fontSize: "0.82rem", color: "var(--text-dim, #64748b)", marginTop: "2px" }}>
                          Check-in: {b.checkIn ? b.checkIn.split("T")[0] : "N/A"} | Check-out: {b.checkOut ? b.checkOut.split("T")[0] : "N/A"}
                        </p>
                      </div>

                      <div style={{ display: "flex", gap: "10px", alignItems: "center" }}>
                        {b.bookingStatus !== "CANCELLED" && b.paymentStatus !== "PAID" && (
                          <button
                            className="item-book-btn"
                            style={{ padding: "6px 12px", fontSize: "0.82rem", background: "linear-gradient(135deg, #10b981 0%, #059669 100%)" }}
                            onClick={() => {
                              startBookingPayment({
                                bookingType: "hotel",
                                bookingId: b.id,
                                userEmail: user?.email,
                                userName: user?.name,
                                userMobile: user?.mobile,
                                onSuccess: () => {
                                  alert("✅ Payment successful! Hotel stay confirmed.");
                                  loadAllBookings(user.id, user.email, localStorage.getItem("token"));
                                },
                                onFailure: (err) => alert("Payment incomplete: " + (err.message || "Failed"))
                              });
                            }}
                          >
                            <CreditCard size={13} /> Pay via UPI / QR
                          </button>
                        )}
                        {b.bookingStatus !== "CANCELLED" && (
                          <button className="booking-cancel-btn" onClick={() => handleCancelHotel(b.id)}>
                            Cancel Stay
                          </button>
                        )}
                      </div>
                    </div>
                  ))}

                {/* 2. FLIGHTS */}
                {(bookingsFilter === "all" || bookingsFilter === "flights") &&
                  flightBookings.map((b) => (
                    <div key={`flt-${b.id}`} className="booking-record-card">
                      <div>
                        <div style={{ display: "flex", alignItems: "center", gap: "8px", marginBottom: "4px" }}>
                          <span className="item-badge-pill"><Plane size={12} /> Flight Ticket</span>
                          <span className={`booking-status-tag ${b.bookingStatus === "CANCELLED" ? "cancelled" : "confirmed"}`}>
                            {b.bookingStatus || "CONFIRMED"}
                          </span>
                        </div>
                        <h4 style={{ fontSize: "1.1rem", fontWeight: 700 }}>Passenger: {b.passengerName}</h4>
                        <p style={{ fontSize: "0.88rem", color: "var(--text-muted, #94a3b8)", marginTop: "4px" }}>
                          Flight ID: #{b.flightId} | Seats: {b.numberOfSeats} | Fare: ₹{b.totalFare}
                        </p>
                        <p style={{ fontSize: "0.82rem", color: "var(--text-dim, #64748b)", marginTop: "2px" }}>
                          Journey Date: {b.journeyDate || "N/A"}
                        </p>
                      </div>

                      <div style={{ display: "flex", gap: "10px", alignItems: "center" }}>
                        {b.bookingStatus !== "CANCELLED" && b.paymentStatus !== "PAID" && (
                          <button
                            className="item-book-btn"
                            style={{ padding: "6px 12px", fontSize: "0.82rem", background: "linear-gradient(135deg, #10b981 0%, #059669 100%)" }}
                            onClick={() => {
                              startBookingPayment({
                                bookingType: "flight",
                                bookingId: b.id,
                                userEmail: user?.email,
                                userName: user?.name,
                                userMobile: user?.mobile,
                                onSuccess: () => {
                                  alert("✅ Payment successful! Flight ticket confirmed.");
                                  loadAllBookings(user.id, user.email, localStorage.getItem("token"));
                                },
                                onFailure: (err) => alert("Payment incomplete: " + (err.message || "Failed"))
                              });
                            }}
                          >
                            <CreditCard size={13} /> Pay via UPI / QR
                          </button>
                        )}
                        {b.bookingStatus !== "CANCELLED" && (
                          <button className="booking-cancel-btn" onClick={() => handleCancelFlight(b.id)}>
                            Cancel Flight
                          </button>
                        )}
                      </div>
                    </div>
                  ))}

                {/* 3. TRAINS */}
                {(bookingsFilter === "all" || bookingsFilter === "trains") &&
                  trainBookings.map((b) => (
                    <div key={`trn-${b.id}`} className="booking-record-card">
                      <div>
                        <div style={{ display: "flex", alignItems: "center", gap: "8px", marginBottom: "4px" }}>
                          <span className="item-badge-pill"><Train size={12} /> Train Ticket</span>
                          <span className={`booking-status-tag ${b.bookingStatus === "CANCELLED" ? "cancelled" : "confirmed"}`}>
                            {b.bookingStatus || "CONFIRMED"}
                          </span>
                        </div>
                        <h4 style={{ fontSize: "1.1rem", fontWeight: 700 }}>Passenger: {b.passengerName}</h4>
                        <p style={{ fontSize: "0.88rem", color: "var(--text-muted, #94a3b8)", marginTop: "4px" }}>
                          Train ID: #{b.trainId} | Tickets: {b.numberOfSeats} | Fare: ₹{b.totalFare}
                        </p>
                        <p style={{ fontSize: "0.82rem", color: "var(--text-dim, #64748b)", marginTop: "2px" }}>
                          Journey Date: {b.journeyDate || "N/A"}
                        </p>
                      </div>

                      <div style={{ display: "flex", gap: "10px", alignItems: "center" }}>
                        {b.bookingStatus !== "CANCELLED" && b.paymentStatus !== "PAID" && (
                          <button
                            className="item-book-btn"
                            style={{ padding: "6px 12px", fontSize: "0.82rem", background: "linear-gradient(135deg, #10b981 0%, #059669 100%)" }}
                            onClick={() => {
                              startBookingPayment({
                                bookingType: "train",
                                bookingId: b.id,
                                userEmail: user?.email,
                                userName: user?.name,
                                userMobile: user?.mobile,
                                onSuccess: () => {
                                  alert("✅ Payment successful! Train ticket confirmed.");
                                  loadAllBookings(user.id, user.email, localStorage.getItem("token"));
                                },
                                onFailure: (err) => alert("Payment incomplete: " + (err.message || "Failed"))
                              });
                            }}
                          >
                            <CreditCard size={13} /> Pay via UPI / QR
                          </button>
                        )}
                        {b.bookingStatus !== "CANCELLED" && (
                          <button className="booking-cancel-btn" onClick={() => handleCancelTrain(b.id)}>
                            Cancel Ticket
                          </button>
                        )}
                      </div>
                    </div>
                  ))}

                {/* 4. BUSES */}
                {(bookingsFilter === "all" || bookingsFilter === "buses") &&
                  busBookings.map((b) => (
                    <div key={`bus-${b.id}`} className="booking-record-card">
                      <div>
                        <div style={{ display: "flex", alignItems: "center", gap: "8px", marginBottom: "4px" }}>
                          <span className="item-badge-pill"><Bus size={12} /> Bus Ticket</span>
                          <span className={`booking-status-tag ${b.bookingStatus === "CANCELLED" ? "cancelled" : "confirmed"}`}>
                            {b.bookingStatus || "CONFIRMED"}
                          </span>
                        </div>
                        <h4 style={{ fontSize: "1.1rem", fontWeight: 700 }}>Passenger: {b.passengerName}</h4>
                        <p style={{ fontSize: "0.88rem", color: "var(--text-muted, #94a3b8)", marginTop: "4px" }}>
                          Bus ID: #{b.busId} | Seats: {b.numberOfSeats} | Fare: ₹{b.totalFare}
                        </p>
                        <p style={{ fontSize: "0.82rem", color: "var(--text-dim, #64748b)", marginTop: "2px" }}>
                          Journey Date: {b.journeyDate || "N/A"}
                        </p>
                      </div>

                      <div style={{ display: "flex", gap: "10px", alignItems: "center" }}>
                        {b.bookingStatus !== "CANCELLED" && b.paymentStatus !== "PAID" && (
                          <button
                            className="item-book-btn"
                            style={{ padding: "6px 12px", fontSize: "0.82rem", background: "linear-gradient(135deg, #10b981 0%, #059669 100%)" }}
                            onClick={() => {
                              startBookingPayment({
                                bookingType: "bus",
                                bookingId: b.id,
                                userEmail: user?.email,
                                userName: user?.name,
                                userMobile: user?.mobile,
                                onSuccess: () => {
                                  alert("✅ Payment successful! Bus ticket confirmed.");
                                  loadAllBookings(user.id, user.email, localStorage.getItem("token"));
                                },
                                onFailure: (err) => alert("Payment incomplete: " + (err.message || "Failed"))
                              });
                            }}
                          >
                            <CreditCard size={13} /> Pay via UPI / QR
                          </button>
                        )}
                        {b.bookingStatus !== "CANCELLED" && (
                          <button className="booking-cancel-btn" onClick={() => handleCancelBus(b.id)}>
                            Cancel Seat
                          </button>
                        )}
                      </div>
                    </div>
                  ))}

                {transactions.length === 0 && (
                  <p style={{ textAlign: "center", padding: "40px", color: "var(--text-muted, #94a3b8)" }}>
                    No bookings found in this category.
                  </p>
                )}
              </div>
            </div>
          )}

          {/* ======================= MENU 5: TRANSACTIONS ======================= */}
          {activeMenu === "transactions" && (
            <div className="dash-card">
              <div className="dash-card-header" style={{ flexWrap: "wrap", gap: "16px", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                  <h2 className="dash-card-title">Transactions Ledger</h2>
                  <p className="dash-card-subtitle">
                    Total Lifetime Spend: <strong style={{ color: "var(--accent-cyan, #38bdf8)" }}>₹{totalSpend}</strong> • Immutable Audit Trail
                  </p>
                </div>
                <div style={{ display: "flex", gap: "10px", alignItems: "center" }}>
                  <span style={{ fontSize: "0.8rem", padding: "4px 10px", borderRadius: "999px", background: "rgba(16, 185, 129, 0.15)", color: "#10b981", border: "1px solid rgba(16, 185, 129, 0.3)" }}>
                    🔒 Undeletable Records
                  </span>
                </div>
              </div>

              {/* Search & Filter Toolbar */}
              <div className="dash-search-toolbar" style={{ display: "flex", flexWrap: "wrap", gap: "12px", margin: "16px 0 24px 0", alignItems: "center" }}>
                <div className="dash-search-box" style={{ flex: "1 1 280px", position: "relative", display: "flex", alignItems: "center" }}>
                  <Search size={18} style={{ position: "absolute", left: "14px", color: "var(--text-muted, #94a3b8)", pointerEvents: "none" }} />
                  <input
                    type="text"
                    placeholder="Search by Txn ID, Order ID, or Service details..."
                    value={txnSearch}
                    onChange={(e) => setTxnSearch(e.target.value)}
                    style={{
                      width: "100%",
                      padding: "10px 38px 10px 42px",
                      background: "rgba(15, 23, 42, 0.6)",
                      border: "1px solid var(--border-glass, rgba(255, 255, 255, 0.1))",
                      borderRadius: "10px",
                      color: "var(--text-main, #ffffff)",
                      fontSize: "0.9rem",
                      outline: "none"
                    }}
                  />
                  {txnSearch && (
                    <button
                      onClick={() => setTxnSearch("")}
                      style={{
                        position: "absolute",
                        right: "10px",
                        background: "transparent",
                        border: "none",
                        color: "var(--text-muted, #94a3b8)",
                        cursor: "pointer",
                        padding: "4px"
                      }}
                      title="Clear search"
                    >
                      <X size={16} />
                    </button>
                  )}
                </div>

                <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
                  <select
                    className="dash-filter-select"
                    value={txnTypeFilter}
                    onChange={(e) => setTxnTypeFilter(e.target.value)}
                    style={{
                      padding: "10px 14px",
                      background: "rgba(15, 23, 42, 0.7)",
                      border: "1px solid var(--border-glass, rgba(255, 255, 255, 0.15))",
                      borderRadius: "10px",
                      color: "var(--text-main, #ffffff)",
                      fontSize: "0.85rem",
                      cursor: "pointer",
                      outline: "none"
                    }}
                  >
                    <option value="ALL">All Services</option>
                    <option value="HOTEL">Hotel Stays</option>
                    <option value="FLIGHT">Flights</option>
                    <option value="TRAIN">Trains</option>
                    <option value="BUS">Buses</option>
                    <option value="TOUR">Tour Packages</option>
                    <option value="CAB">Cab Bookings</option>
                  </select>

                  <select
                    className="dash-filter-select"
                    value={txnStatusFilter}
                    onChange={(e) => setTxnStatusFilter(e.target.value)}
                    style={{
                      padding: "10px 14px",
                      background: "rgba(15, 23, 42, 0.7)",
                      border: "1px solid var(--border-glass, rgba(255, 255, 255, 0.15))",
                      borderRadius: "10px",
                      color: "var(--text-main, #ffffff)",
                      fontSize: "0.85rem",
                      cursor: "pointer",
                      outline: "none"
                    }}
                  >
                    <option value="ALL">All Statuses</option>
                    <option value="PAID">PAID</option>
                    <option value="CONFIRMED">CONFIRMED</option>
                    <option value="PENDING">PENDING</option>
                    <option value="FAILED">FAILED</option>
                  </select>
                </div>
              </div>

              <div className="table-responsive-wrapper">
                <table className="dash-data-table">
                  <thead>
                    <tr>
                      <th>Txn ID</th>
                      <th>Service Type</th>
                      <th>Description</th>
                      <th>Method</th>
                      <th>Date</th>
                      <th>Amount</th>
                      <th>Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredTransactions.map((tx, idx) => (
                      <tr key={idx}>
                        <td style={{ fontFamily: "monospace", fontSize: "0.85rem", color: "var(--text-muted, #94a3b8)" }}>
                          {tx.id}
                        </td>
                        <td>
                          <span className="item-badge-pill">{tx.type}</span>
                        </td>
                        <td>{tx.details}</td>
                        <td style={{ fontSize: "0.82rem", color: "var(--text-muted, #94a3b8)" }}>
                          {tx.method || "UPI / QR"}
                        </td>
                        <td>{tx.date}</td>
                        <td style={{ fontWeight: 800, color: "var(--text-main, #ffffff)" }}>₹{tx.amount}</td>
                        <td>
                          <span className={`booking-status-tag ${(tx.status || "CONFIRMED").toLowerCase()}`}>
                            {tx.status}
                          </span>
                        </td>
                        <td>
                          <button
                            className="category-tab-btn"
                            style={{ padding: "4px 8px", fontSize: "0.75rem" }}
                            onClick={() => setSelectedReceipt(tx)}
                          >
                            <Receipt size={13} /> Receipt
                          </button>
                        </td>
                      </tr>
                    ))}
                    {filteredTransactions.length === 0 && (
                      <tr>
                        <td colSpan={8} style={{ textAlign: "center", padding: "35px", color: "var(--text-muted, #94a3b8)" }}>
                          No transaction records found matching your filter criteria.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* ======================= MENU 6: FAVORITES ======================= */}
          {activeMenu === "favorites" && (
            <div className="dash-card">
              <div className="dash-card-header">
                <div>
                  <h2 className="dash-card-title">Saved Favorites</h2>
                  <p className="dash-card-subtitle">Hotels and stays saved to your personal wishlist</p>
                </div>
              </div>

              <div className="dash-items-grid">
                {favorites.map((hotel) => (
                  <div key={hotel.id} className="dash-item-card">
                    <div>
                      <div className="item-card-top">
                        <div>
                          <h4 className="item-title">{hotel.hotel}</h4>
                          <span className="item-badge-pill">
                            <MapPin size={12} /> {hotel.city}
                          </span>
                        </div>
                        <button
                          className="item-favorite-btn"
                          onClick={() => toggleFavorite(hotel)}
                          title="Remove from favorites"
                        >
                          <Heart size={20} fill="#ef4444" />
                        </button>
                      </div>

                      <div className="item-details-list" style={{ marginTop: "12px" }}>
                        <div className="item-details-row">
                          <strong>📍 Location:</strong>
                          <span>{hotel.location || hotel.address || hotel.city}</span>
                        </div>
                        <div className="item-details-row">
                          <strong>🏨 Rooms:</strong>
                          <span>{hotel.roomavl || hotel.availableRooms || 5}</span>
                        </div>
                      </div>
                    </div>

                    <div className="item-card-actions">
                      <div className="item-price-tag">
                        ₹{hotel.price} <span style={{ fontSize: "0.8rem", color: "var(--text-muted, #94a3b8)" }}>/ night</span>
                      </div>
                      <button
                        className="item-book-btn"
                        onClick={() => openBookingModal("hotel", hotel)}
                      >
                        Book Now
                      </button>
                    </div>
                  </div>
                ))}
                {favorites.length === 0 && (
                  <div style={{ gridColumn: "1/-1", textAlign: "center", padding: "40px", color: "var(--text-muted, #94a3b8)" }}>
                    <Heart size={36} style={{ opacity: 0.4, marginBottom: "10px" }} />
                    <p>No favorites saved yet. Click the heart icon on any hotel card in Search & Book to save it here!</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* ======================= MENU 7: SUPPORT ======================= */}
          {activeMenu === "support" && (
            <div className="dash-card">
              <div className="dash-card-header">
                <div>
                  <h2 className="dash-card-title">Support & Helpdesk</h2>
                  <p className="dash-card-subtitle">Submit queries, report issues, or contact NextGem Technology directly</p>
                </div>
              </div>

              <div className="support-grid-2col">
                {/* Left: Submit Query */}
                <div>
                  <h3 style={{ fontSize: "1.1rem", marginBottom: "12px" }}>Submit a Support Query</h3>
                  <form onSubmit={handleSubmitComplaint} style={{ display: "flex", flexDirection: "column", gap: "14px" }}>
                    <textarea
                      rows={5}
                      className="auth-input"
                      placeholder="Describe your issue, booking query, or feedback in detail..."
                      value={newComplaint.message}
                      onChange={(e) => setNewComplaint({ message: e.target.value })}
                      required
                    />
                    <button type="submit" className="dash-search-btn" disabled={submittingComplaint}>
                      {submittingComplaint ? "Submitting..." : "Send Ticket to Support"}
                    </button>
                  </form>

                  {/* Past Submitted Tickets */}
                  <h4 style={{ fontSize: "1rem", marginTop: "24px", marginBottom: "10px" }}>My Submitted Tickets</h4>
                  <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                    {complaints.map((c, i) => (
                      <div key={i} style={{
                        background: "var(--bg-input, rgba(255,255,255,0.05))",
                        border: "1px solid var(--border-color, rgba(255,255,255,0.08))",
                        padding: "12px 14px",
                        borderRadius: "10px"
                      }}>
                        <div style={{ display: "flex", justifyContent: "space-between", fontSize: "0.8rem", color: "var(--accent-cyan, #38bdf8)", fontWeight: 600 }}>
                          <span>Ticket #{c.id || i + 1}</span>
                          <span>{c.status || "RECEIVED"}</span>
                        </div>
                        <p style={{ fontSize: "0.9rem", marginTop: "6px" }}>{c.message}</p>
                      </div>
                    ))}
                    {complaints.length === 0 && (
                      <p style={{ fontSize: "0.85rem", color: "var(--text-muted, #94a3b8)" }}>
                        No support tickets opened yet.
                      </p>
                    )}
                  </div>
                </div>

                {/* Right: Direct Contact & NextGem Links */}
                <div className="support-contact-box">
                  <h3 style={{ fontSize: "1.1rem" }}>NEXTGEM-TECHNOLOGY Support</h3>
                  <p style={{ fontSize: "0.88rem", color: "var(--text-muted, #94a3b8)" }}>
                    Our customer experience team is available 24/7 to assist with your stays, bookings, and payments.
                  </p>

                  <div style={{ display: "flex", flexDirection: "column", gap: "12px", marginTop: "6px" }}>
                    <a href="mailto:hotelluxnes@gmail.com" className="support-link-item">
                      ✉️ Email: hotelluxnes@gmail.com
                    </a>
                    <a href="https://nextgem-technology.web.app/" target="_blank" rel="noopener noreferrer" className="support-link-item">
                      🌐 Official Portal: nextgem-technology.web.app <ExternalLink size={14} />
                    </a>
                    <a href="https://www.linkedin.com/company/139843904/admin/dashboard/" target="_blank" rel="noopener noreferrer" className="support-link-item">
                      💼 LinkedIn: NextGem Technology <ExternalLink size={14} />
                    </a>
                    <a href="https://www.instagram.com/nextgemtechnology/" target="_blank" rel="noopener noreferrer" className="support-link-item">
                      📸 Instagram: @nextgemtechnology <ExternalLink size={14} />
                    </a>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* ======================= MENU 8: SETTINGS ======================= */}
          {activeMenu === "settings" && (
            <div className="dash-card">
              <div className="dash-card-header">
                <div>
                  <h2 className="dash-card-title">Account Settings</h2>
                  <p className="dash-card-subtitle">Customize theme preferences and manage credentials</p>
                </div>
              </div>

              {/* Theme Selector */}
              <div style={{ marginBottom: "32px" }}>
                <h3 style={{ fontSize: "1.05rem", marginBottom: "6px" }}>Display Theme (Persistent)</h3>
                <p style={{ fontSize: "0.85rem", color: "var(--text-muted, #94a3b8)" }}>
                  Choose your preferred appearance across all pages and sessions.
                </p>

                <div className="theme-picker-cards">
                  {Object.values(THEME_CONFIG).map((tm) => (
                    <div
                      key={tm.id}
                      className={`theme-mode-card ${currentTheme === tm.id ? "active" : ""}`}
                      onClick={() => handleThemeSelect(tm.id)}
                    >
                      <div style={{ fontSize: "1.6rem" }}>{tm.icon}</div>
                      <div style={{ fontWeight: 700, fontSize: "0.95rem" }}>{tm.label}</div>
                      <div style={{ fontSize: "0.78rem", color: "var(--text-muted, #94a3b8)" }}>{tm.desc}</div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Change Password Form */}
              <div style={{ marginBottom: "32px" }}>
                <h3 style={{ fontSize: "1.05rem", marginBottom: "14px" }}>Change Security Password</h3>
                <form onSubmit={handleChangePassword} style={{ maxWidth: "460px", display: "flex", flexDirection: "column", gap: "12px" }}>
                  <div className="password-input-wrapper">
                    <input
                      type={showPassword ? "text" : "password"}
                      className="auth-input"
                      placeholder="Current Password"
                      value={passwordForm.currentPassword}
                      onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
                      required
                    />
                    <button
                      type="button"
                      className="password-toggle-btn"
                      onClick={() => setShowPassword(!showPassword)}
                    >
                      {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>

                  <input
                    type={showPassword ? "text" : "password"}
                    className="auth-input"
                    placeholder="New Password (min 6 characters)"
                    value={passwordForm.newPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                    required
                  />

                  <input
                    type={showPassword ? "text" : "password"}
                    className="auth-input"
                    placeholder="Confirm New Password"
                    value={passwordForm.confirmPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                    required
                  />

                  <button type="submit" className="dash-search-btn" disabled={savingPassword}>
                    {savingPassword ? "Updating..." : "Update Password"}
                  </button>
                </form>
              </div>

              {/* Danger Zone: Deactivate Account */}
              <div style={{ borderTop: "1px solid rgba(239, 68, 68, 0.2)", paddingTop: "20px" }}>
                <h3 style={{ fontSize: "1.05rem", color: "var(--accent-red, #ef4444)", marginBottom: "6px" }}>
                  Danger Zone: Account Deactivation
                </h3>
                <p style={{ fontSize: "0.85rem", color: "var(--text-muted, #94a3b8)", marginBottom: "14px" }}>
                  Deactivating your account will lock your profile and invalidate active tokens.
                </p>
                <button
                  className="booking-cancel-btn"
                  onClick={() => setDisableModalOpen(true)}
                >
                  Deactivate My Account
                </button>
              </div>
            </div>
          )}

        </main>
      </div>

      {/* ======================= BOOKING CONFIRMATION MODAL ======================= */}
      {activeModal && (
        <div className="dash-modal-overlay">
          <div className="dash-modal-dialog">
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "18px" }}>
              <h3 style={{ fontSize: "1.25rem", margin: 0 }}>
                Confirm {activeModal.type.toUpperCase()} Booking
              </h3>
              <button
                style={{ background: "none", border: "none", color: "var(--text-muted, #94a3b8)", cursor: "pointer" }}
                onClick={() => setActiveModal(null)}
              >
                <X size={20} />
              </button>
            </div>

            <div style={{ marginBottom: "16px", padding: "12px", background: "var(--bg-input, rgba(255,255,255,0.06))", borderRadius: "10px" }}>
              <div style={{ fontWeight: 700, fontSize: "1.05rem" }}>
                {activeModal.item.hotel || activeModal.item.airline || activeModal.item.trainName || activeModal.item.operatorName}
              </div>
              <div style={{ color: "var(--accent-cyan, #38bdf8)", fontWeight: 800, fontSize: "1.1rem", marginTop: "4px" }}>
                Price / Fare: ₹{activeModal.item.price}
              </div>
            </div>

            {activeModal.type !== "hotel" ? (
              <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
                <div>
                  <label style={{ fontSize: "0.82rem", fontWeight: 600, display: "block", marginBottom: "4px" }}>Passenger Name</label>
                  <input
                    type="text"
                    className="auth-input"
                    value={bookingFormData.passengerName}
                    onChange={(e) => setBookingFormData({ ...bookingFormData, passengerName: e.target.value })}
                    required
                  />
                </div>

                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "10px" }}>
                  <div>
                    <label style={{ fontSize: "0.82rem", fontWeight: 600, display: "block", marginBottom: "4px" }}>Age</label>
                    <input
                      type="number"
                      className="auth-input"
                      value={bookingFormData.passengerAge}
                      onChange={(e) => setBookingFormData({ ...bookingFormData, passengerAge: e.target.value })}
                      required
                    />
                  </div>
                  <div>
                    <label style={{ fontSize: "0.82rem", fontWeight: 600, display: "block", marginBottom: "4px" }}>Gender</label>
                    <select
                      className="auth-input"
                      value={bookingFormData.passengerGender}
                      onChange={(e) => setBookingFormData({ ...bookingFormData, passengerGender: e.target.value })}
                    >
                      <option value="Male">Male</option>
                      <option value="Female">Female</option>
                      <option value="Other">Other</option>
                    </select>
                  </div>
                </div>

                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "10px" }}>
                  <div>
                    <label style={{ fontSize: "0.82rem", fontWeight: 600, display: "block", marginBottom: "4px" }}>Seats</label>
                    <input
                      type="number"
                      min="1"
                      className="auth-input"
                      value={bookingFormData.numberOfSeats}
                      onChange={(e) => setBookingFormData({ ...bookingFormData, numberOfSeats: e.target.value })}
                      required
                    />
                  </div>
                  <div>
                    <label style={{ fontSize: "0.82rem", fontWeight: 600, display: "block", marginBottom: "4px" }}>Journey Date</label>
                    <input
                      type="date"
                      className="auth-input"
                      value={bookingFormData.journeyDate}
                      onChange={(e) => setBookingFormData({ ...bookingFormData, journeyDate: e.target.value })}
                      required
                    />
                  </div>
                </div>
              </div>
            ) : (
              <p style={{ fontSize: "0.9rem", color: "var(--text-muted, #94a3b8)" }}>
                Click confirm to reserve your room at {activeModal.item.hotel}. Confirmation will be saved immediately to your account bookings.
              </p>
            )}

            <div style={{ display: "flex", gap: "10px", marginTop: "20px" }}>
              <button
                className="item-book-btn"
                onClick={handleConfirmBooking}
                disabled={submittingBooking}
              >
                {submittingBooking ? "Confirming..." : "Confirm Reservation"}
              </button>
              <button
                className="category-tab-btn"
                onClick={() => setActiveModal(null)}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ======================= RECEIPT MODAL ======================= */}
      {selectedReceipt && (
        <div className="dash-modal-overlay">
          <div className="dash-modal-dialog">
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "16px" }}>
              <h3 style={{ margin: 0 }}>Booking Receipt</h3>
              <button
                style={{ background: "none", border: "none", color: "var(--text-muted, #94a3b8)", cursor: "pointer" }}
                onClick={() => setSelectedReceipt(null)}
              >
                <X size={20} />
              </button>
            </div>

            <div style={{ border: "1px solid var(--border-color, rgba(255,255,255,0.1))", borderRadius: "12px", padding: "16px", background: "var(--bg-input, rgba(255,255,255,0.04))" }}>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "10px" }}>
                <span style={{ fontSize: "0.8rem", color: "var(--text-muted, #94a3b8)" }}>RECEIPT ID</span>
                <span style={{ fontFamily: "monospace", fontWeight: 700 }}>{selectedReceipt.id}</span>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "10px" }}>
                <span style={{ fontSize: "0.8rem", color: "var(--text-muted, #94a3b8)" }}>SERVICE</span>
                <span style={{ fontWeight: 600 }}>{selectedReceipt.type}</span>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "10px" }}>
                <span style={{ fontSize: "0.8rem", color: "var(--text-muted, #94a3b8)" }}>DETAILS</span>
                <span style={{ fontSize: "0.85rem", maxWidth: "200px", textAlign: "right" }}>{selectedReceipt.details}</span>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "10px" }}>
                <span style={{ fontSize: "0.8rem", color: "var(--text-muted, #94a3b8)" }}>DATE</span>
                <span>{selectedReceipt.date}</span>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", borderTop: "1px solid var(--border-color, rgba(255,255,255,0.1))", paddingTop: "10px" }}>
                <strong style={{ fontSize: "1.1rem" }}>TOTAL PAID:</strong>
                <strong style={{ fontSize: "1.15rem", color: "var(--accent-cyan, #38bdf8)" }}>₹{selectedReceipt.amount}</strong>
              </div>
            </div>

            <div style={{ display: "flex", justifyContent: "flex-end", marginTop: "16px" }}>
              <button className="dash-search-btn" onClick={() => window.print()}>
                Print / Save PDF
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ======================= DISABLE ACCOUNT CONFIRMATION MODAL ======================= */}
      {disableModalOpen && (
        <div className="dash-modal-overlay">
          <div className="dash-modal-dialog">
            <h3 style={{ color: "var(--accent-red, #ef4444)", marginBottom: "10px" }}>
              Confirm Account Deactivation
            </h3>
            <p style={{ fontSize: "0.88rem", color: "var(--text-muted, #94a3b8)", marginBottom: "16px" }}>
              This will lock your account ({user?.email}) and prevent future logins. Enter your password to proceed:
            </p>
            <input
              type="password"
              className="auth-input"
              placeholder="Enter current password"
              value={disablePassword}
              onChange={(e) => setDisablePassword(e.target.value)}
              style={{ marginBottom: "16px" }}
            />
            <div style={{ display: "flex", gap: "10px" }}>
              <button
                className="booking-cancel-btn"
                onClick={handleDisableAccount}
                disabled={disablingAccount}
              >
                {disablingAccount ? "Deactivating..." : "Yes, Deactivate Account"}
              </button>
              <button
                className="category-tab-btn"
                onClick={() => { setDisableModalOpen(false); setDisablePassword(""); }}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 3. Docked Footer */}
      <Footer />
    </div>
  );
}

export default Dashboard;