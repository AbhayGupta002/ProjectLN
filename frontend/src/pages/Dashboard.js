import axios from "axios";
import React, { useEffect, useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { User } from "lucide-react";
import "../styles/Dashboard.css";



function Dashboard() {
const [openPanel, setOpenPanel] = useState(""); // "" means no panel is open
  const navigate = useNavigate();
  const dropdownRef = useRef();
  const hotelTrackRef = useRef();
  const [showProfilePanel, setShowProfilePanel] = useState(false);

  /* ------------------- STATE ------------------- */
  const [openPanels, setOpenPanels] = useState({
    hotels: false,
    bookings: false,
    offers: false,
    history: false,
    transaction: false,
    nearby: false,
    feedback: false,
  });
  const [profile, setProfile] = useState(null);
  const [user, setUser] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const [activities, setActivities] = useState([]);
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [showActivities, setShowActivities] = useState(true);
  const [loading, setLoading] = useState(true);

  const [activeTab, setActiveTab] = useState("overview");
  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState({});
  const [saving, setSaving] = useState(false);
  const [passwordForm, setPasswordForm] = useState({ currentPassword: "", newPassword: "", confirmPassword: "" });
  const [open, setOpen] = useState(false);
  const [bookings, setBookings] = useState([]);
  const [hotels, setHotels] = useState([]);
  const [tours, setTours] = useState([]);
  const [tourLocation, setTourLocation] = useState("");
  const [tourDays, setTourDays] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [isOpen, setIsOpen] = useState(false);

{/*top hotels*/}
  useEffect(() => {
    fetchTopHotels();
  }, []);

  const fetchTopHotels = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/public/tophotels");

      console.log("FULL RESPONSE:", res.data);

      const hotelsData = res.data.body?.data || [];

      setHotels(hotelsData);

    } catch (err) {
      console.error(err);
    }
  };

  /* ------------------- LOAD PROFILE & DATA ------------------- */
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) { navigate("/login"); return; }

    // Fetch profile
    const fetchProfile = async () => {
      try {
        const res = await axios.get("http://localhost:8080/api/dashboard/profile", {
          headers: { Authorization: `Bearer ${token}` }
        });
        setProfile(res.data);
        setEditForm(res.data);
        setUser(res.data);
      } catch (err) {
        console.error(err);
        navigate("/login");
      } finally { setLoading(false); }
    };

    // Fetch notifications and activities
    axios.get("/api/user/notifications", { headers: { Authorization: `Bearer ${token}` } })
      .then(res => setNotifications(res.data))
      .catch(err => console.error(err));

    axios.get("/api/user/activity", { headers: { Authorization: `Bearer ${token}` } })
      .then(res => setActivities(res.data))
      .catch(err => console.error(err));

    // Fetch bookings
    axios.get("http://localhost:8080/api/bookings/my-bookings", { headers: { Authorization: `Bearer ${token}` } })
      .then(res => setBookings(res.data?.data || []))
      .catch(err => console.error(err));

    fetchProfile();
  }, [navigate]);

  /* ------------------- DROPDOWN CLOSE ------------------- */
  useEffect(() => {
    const handler = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  /* ------------------- HANDLERS ------------------- */
  const handleEditToggle = () => { if (isEditing) setEditForm(profile); setIsEditing(!isEditing); };
  const handleInputChange = (e) => setEditForm({ ...editForm, [e.target.name]: e.target.value });

  const handleSaveProfile = async () => {
    setSaving(true);
    try {
      const token = localStorage.getItem("token");
      const res = await axios.put("http://localhost:8080/api/dashboard/update-profile", editForm, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setProfile(res.data);
      setIsEditing(false);
      alert("Profile updated!");
    } catch (err) { alert("Failed to update profile"); }
    finally { setSaving(false); }
  };

  const handlePasswordChangeInput = (e) => setPasswordForm({ ...passwordForm, [e.target.name]: e.target.value });
  const handleChangePassword = async (e) => {
    e.preventDefault();
    if (passwordForm.newPassword !== passwordForm.confirmPassword) { alert("Passwords do not match!"); return; }
    try {
      const token = localStorage.getItem("token");
      await axios.put("http://localhost:8080/api/dashboard/change-password", {
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword
      }, { headers: { Authorization: `Bearer ${token}` } });
      alert("Password changed!");
      setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
    } catch (err) { alert("Failed to change password"); }
  };

  const handleLogout = () => { localStorage.removeItem("token"); navigate("/login"); };

  const handleSearchTours = async () => {
    try {
      const token = localStorage.getItem("token");
      if (!tourLocation && !tourDays) { alert("Enter location or days"); return; }

      let res;
      if (tourLocation) {
        res = await axios.get("http://localhost:8080/api/public/location", { params: { location: tourLocation }, headers: { Authorization: `Bearer ${token}` } });
      } else {
        res = await axios.get("http://localhost:8080/api/public/days", { params: { days: tourDays }, headers: { Authorization: `Bearer ${token}` } });
      }
      setSearchResults(res.data?.data || []);
    } catch (err) { console.error(err); alert("Error fetching tours"); }
  };

  const pauseHotelScroll = (pause) => {
    if (!hotelTrackRef.current) return;
    hotelTrackRef.current.style.animationPlayState = pause ? "paused" : "running";
  };

  /* ------------------- RENDER ------------------- */
  if (loading) return <p style={{ padding: 40 }}>Loading dashboard...</p>;
  if (!profile) return <p style={{ padding: 40 }}>No profile found.</p>;

  return (
    <div className={`dashboard ${sidebarOpen ? "sidebar-open" : ""}`}>
      {/* SIDEBAR */}
      <aside className="sidebar">
        <div className="sidebar-top">
          <div className="sidebar-logo">MyPortal</div>
          <button className="toggle-btn" onClick={() => setSidebarOpen(!sidebarOpen)}>☰</button>
        </div>
        <nav>
          <ul>
            <li>Dashboard</li>
            <li onClick={() => setShowProfilePanel(true)}>Profile</li>
               <li onClick={() => setOpenPanel(openPanel === "hotels" ? "" : "hotels")}>Hotels</li>
                <li onClick={() => setOpenPanel(openPanel === "bookings" ? "" : "bookings")}>My-Bookings</li>
                <li onClick={() => setOpenPanel(openPanel === "offers" ? "" : "offers")}>Offers</li>
                <li onClick={() => setOpenPanel(openPanel === "history" ? "" : "history")}>History</li>
                <li onClick={() => setOpenPanel(openPanel === "transaction" ? "" : "transaction")}>Transaction</li>
                <li onClick={() => setOpenPanel(openPanel === "nearby" ? "" : "nearby")}>Hotel-NearBy</li>
                <li onClick={() => setOpenPanel(openPanel === "feedback" ? "" : "feedback")}>Feedback</li>
            <li onClick={handleLogout}>Logout</li>
          </ul>
        </nav>
      </aside>

      {/* MAIN CONTENT */}
      <main className="main-content">
        <header className="header">
          <h1>Welcome, {user ? user.name : "User"}</h1>


          {/* 🔥 FEATURED HOTELS
              <section className="dash-hotels-section">
                <h2 className="dash-hotels-title">Featured Hotels</h2>

                <div className="dash-hotels-container">
                  {hotels.length > 0 ? (
                    hotels.map((hotel, index) => (
                      <div className="dash-hotel-card" key={hotel.id}>

                        <img
                          className="dash-hotel-img"
                          src={`/images/hotel${(index % 3) + 1}.jpg`}
                          alt="hotel"
                        />

                        <div className="dash-hotel-content">
                          <h3 className="dash-hotel-name">{hotel.hotel}</h3>

                          <p className="dash-hotel-city">{hotel.city}</p>

                          <p className="dash-hotel-price">₹{hotel.price} / night</p>

                          <button className="dash-book-btn">Book Now</button>
                        </div>

                      </div>
                    ))
                  ) : (
                    <p className="dash-no-data">No hotels found</p>
                  )}
                </div>
              </section>
              */}


          {/* PROFILE DROPDOWN
          <div className="dropdown" ref={dropdownRef}>
            <button className="profile-btn" onClick={() => setOpen(!open)}><User size={20} /></button>
            {open && (
              <div className="dropdown-menu">
                <button onClick={() => { setActiveTab("edit-profile"); setOpen(false); }}>Edit Profile</button>
                <button onClick={() => { setActiveTab("security"); setOpen(false); }}>Security</button>
                <button onClick={() => { setActiveTab("bookings"); setOpen(false); }}>Bookings</button>
                <button onClick={() => { setActiveTab("tours"); setOpen(false); }}>Search Tours</button>
                <button onClick={handleLogout}>Logout</button>
              </div>
            )}
          </div>
          */}
          {/* HOTELS PANEL */}
          {openPanel === "hotels" && (
            <div className="glass-card">
              <h3>Hotels</h3>
              <button className="cancel-btn" onClick={() => setOpenPanel("")}>Close</button>
              {/* Render your hotels list here */}
            </div>
          )}

          {/* BOOKINGS PANEL */}
          {openPanel === "bookings" && (
            <div className="glass-card">
              <h3>My Bookings</h3>
              <button className="cancel-btn" onClick={() => setOpenPanel("")}>Close</button>
              {bookings.length === 0 ? <p>No bookings</p> :
                bookings.map((b, i) => (
                  <div className="booking-card" key={i}>
                    <p><b>ID:</b> {b.id}</p>
                    <p><b>Hotel:</b> {b.hotelName}</p>
                    <p><b>Check-in:</b> {b.checkIn}</p>
                    <p><b>Check-out:</b> {b.checkOut}</p>
                  </div>
                ))
              }
            </div>
          )}

          {/* OFFERS PANEL */}
          {openPanel === "offers" && (
            <div className="glass-card">
              <h3>Offers</h3>
              <button className="cancel-btn" onClick={() => setOpenPanel("")}>Close</button>
              {/* Render your offers here */}
            </div>
          )}

          {/* HISTORY PANEL */}
          {openPanel === "history" && (
            <div className="glass-card">
              <h3>History</h3>
              <button className="cancel-btn" onClick={() => setOpenPanel("")}>Close</button>
              {/* Render history */}
            </div>
          )}

          {/* TRANSACTION PANEL */}
          {openPanel === "transaction" && (
            <div className="glass-card">
              <h3>Transaction</h3>
              <button className="cancel-btn" onClick={() => setOpenPanel("")}>Close</button>
              {/* Render transactions */}
            </div>
          )}

          {/* NEARBY HOTELS PANEL */}
          {openPanel === "nearby" && (
            <div className="glass-card">
              <h3>Hotel NearBy</h3>
              <button className="cancel-btn" onClick={() => setOpenPanel("")}>Close</button>
              {/* Render nearby hotels */}
            </div>
          )}

          {/* FEEDBACK PANEL */}
          {openPanel === "feedback" && (
            <div className="glass-card">
              <h3>Feedback</h3>
              <button className="cancel-btn" onClick={() => setOpenPanel("")}>Close</button>
              {/* Feedback form or list */}
            </div>
          )}

          <div className="profile-section">
            <img src={user?.avatar || "https://i.pravatar.cc/40"} alt="Avatar" className="avatar" />
            <span>{user?.email || "Guest"}</span>
          </div>
        </header>

        {/* AI ASSISTANT
        <div className="ai-assistant" onClick={() => setIsOpen(true)}>🤖</div>
        {isOpen && <AIChatModal onClose={() => setIsOpen(false)} />}
        */}

        {/* MAIN GRID */}
        <div className="main-grid">
          {/* LEFT COLUMN */}
          <div className="col-left">

            {/* EDIT PROFILE */}
            {showProfilePanel && (
              <div className="glass-card">
                <h3>Edit Profile</h3>
                <button className="cancel-btn" onClick={() => setShowProfilePanel(false)}>Close</button>

                {!isEditing ? (
                  <button onClick={handleEditToggle}>Edit</button>
                ) : (
                  <>
                    <button onClick={handleSaveProfile}>{saving ? "Saving..." : "Save"}</button>
                    <button onClick={handleEditToggle} className="cancel-btn">Cancel</button>
                  </>
                )}

                {["name","mobile","city"].map(f => (
                  <div className="form-group" key={f}>
                    <label>{f.charAt(0).toUpperCase()+f.slice(1)}:</label>
                    <input name={f} value={editForm[f] || ""} disabled={!isEditing} onChange={handleInputChange} />
                  </div>
                ))}

                <div className="form-group">
                  <label>Email:</label>
                  <input value={editForm.email || ""} disabled />
                </div>
              </div>
            )}

            {/* BOOKINGS */}
            {activeTab === "bookings" && (
              <div className="glass-card">
                <h3>My Bookings</h3>
                {bookings.length===0 ? <p>No bookings</p> :
                  bookings.map((b,i)=>(
                    <div className="booking-card" key={i}>
                      <p><b>ID:</b> {b.id}</p>
                      <p><b>Hotel:</b> {b.hotelName}</p>
                      <p><b>Check-in:</b> {b.checkIn}</p>
                      <p><b>Check-out:</b> {b.checkOut}</p>
                    </div>
                  ))
                }
              </div>
            )}

            {/* TOURS SEARCH */}
            {activeTab === "tours" && (
              <div className="glass-card">
                <h3>Search Tours</h3>
                <div className="form-group">
                  <input placeholder="Location" value={tourLocation} onChange={(e)=>setTourLocation(e.target.value)} />
                  <input type="number" placeholder="Days" value={tourDays} onChange={(e)=>setTourDays(e.target.value)} />
                </div>
                <button onClick={handleSearchTours}>Search</button>

                {searchResults.length>0 && (
                  <div className="search-results">
                    <button className="close-btn" onClick={()=>setSearchResults([])}>×</button>
                    {searchResults.map(t=>(
                      <div className="tour-card" key={t.id}>
                        <p><b>{t.title}</b> ({t.durationDays} days) - ₹{t.price}</p>
                        <p><b>Location:</b> {t.location}</p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>

          {/* RIGHT COLUMN */}
          <div className="col-right">
            {/* Popular Tours */}


            {/* Hotels */}


          </div>
        </div>
      </main>
    </div>
  );
}

export default Dashboard;