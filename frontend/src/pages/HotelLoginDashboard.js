import React, { useEffect, useState, useRef } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import "../styles/HotelLoginDashboard.css";

import { getHotelBookings } from "../api/bookingApi";
import { updateHotelProfile, getDashboardStats } from "../api/hotelDashboardApi";
import { createTour, getAllTourPackages } from "../api/TourPackageApi";

function HotelLoginDashboard() {
  const navigate = useNavigate();

  const [profile, setProfile] = useState({});
  const [loading, setLoading] = useState(true);

  const [bookings, setBookings] = useState([]);
  const [totalBookings, setTotalBookings] = useState(0);
  const [loadingBookings, setLoadingBookings] = useState(false);

  const [tours, setTours] = useState([]);

  const [activeTab, setActiveTab] = useState("dashboard");

  const [showUpdateForm, setShowUpdateForm] = useState(false);
  const [showCreateTour, setShowCreateTour] = useState(false);

  const modalRef = useRef();

  const [stats, setStats] = useState({
    totalTours: 0,
    totalBookings: 0,
    tourBookings: 0,
    revenue: 0,
    pending: 0,
  });

  const [tourData, setTourData] = useState({
    title: "",
    description: "",
    location: "",
    price: "",
    durationDays: "",
    imageUrl: "",
  });

  const token =
    localStorage.getItem("hotelToken") ||
    localStorage.getItem("token");

  // 🔐 Redirect if not logged in
  useEffect(() => {
    if (!token) navigate("/hotel-login");
  }, [token, navigate]);

  // 📌 CLOSE MODAL ON OUTSIDE CLICK
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (modalRef.current && !modalRef.current.contains(e.target)) {
        setShowCreateTour(false);
        setShowUpdateForm(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // 👤 FETCH PROFILE
  useEffect(() => {
    if (!token) return;

    const fetchProfile = async () => {
      try {
        const res = await axios.get(
          `${process.env.REACT_APP_API_URL || "http://localhost:8080"}/api/hotellogindashboard/hotelprofile`,
          { headers: { Authorization: `Bearer ${token}` } }
        );

        setProfile(res.data || {});
      } catch (err) {
        localStorage.clear();
        navigate("/hotel-login");
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [token, navigate]);

  // 📊 FETCH STATS
  useEffect(() => {
    const fetchStats = async () => {
      try {
        const res = await getDashboardStats();
        setStats(res.data.data);
      } catch (err) {
        console.error(err);
      }
    };
    fetchStats();
  }, []);

  // 📦 FETCH BOOKINGS
  const fetchBookings = async () => {
    try {
      setLoadingBookings(true);

      const res = await getHotelBookings();
      const data = res.data?.data;

      setBookings(data?.bookings || []);
      setTotalBookings(data?.totalBookings || 0);
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingBookings(false);
    }
  };

  // 🎯 FETCH TOURS
  const fetchTours = async () => {
    try {
      const res = await getAllTourPackages();
      setTours(res.data?.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) return <h2 style={{ textAlign: "center" }}>Loading...</h2>;

  return (
    <div className="hotel-dashboard-container">
      {/* SIDEBAR */}
      <div className="sidebar">
        <h2>Hotel Panel</h2>
        <ul>
          <li onClick={() => setActiveTab("dashboard")}>Dashboard</li>

          <li onClick={() => setShowUpdateForm(true)}>
            Update Profile
          </li>

          <li onClick={() => setShowCreateTour(true)}>
            Create Tour
          </li>

          <li
            onClick={() => {
              setActiveTab("bookings");
              fetchBookings();
            }}
          >
            Bookings
          </li>

          <li
            onClick={() => {
              setActiveTab("tours");
              fetchTours();
            }}
          >
            My Tours
          </li>

          <li
            onClick={() => {
              localStorage.clear();
              navigate("/hotel-login");
            }}
          >
            Logout
          </li>
        </ul>
      </div>

      {/* MAIN */}
      <div className="main">
        {/* NAVBAR */}
        <div className="navbar">
          <u><h2>Welcome, {profile.hotel}</h2></u>
          <div className="profile">{profile.email}</div>
        </div>

        {/* DASHBOARD */}
        {activeTab === "dashboard" && (
          <>
            <div className="cards">
              <div className="card">
                <h3>Total Tours</h3>
                <p>{stats.totalTours}</p>
              </div>

              <div className="card">
                <h3>Hotel Bookings</h3>
                <p>{stats.totalBookings}</p>
              </div>

              <div className="card">
                <h3>Tour Bookings</h3>
                <p>{stats.tourBookings}</p>
              </div>

              <div className="card">
                <h3>Revenue</h3>
                <p>₹{stats.revenue}</p>
              </div>

              <div className="card">
                <h3>Pending</h3>
                <p>{stats.pending}</p>
              </div>
            </div>
          </>
        )}

        {/* BOOKINGS */}
        {activeTab === "bookings" && (
          <div className="table-section">
            <h3>Bookings ({totalBookings})</h3>

            {loadingBookings ? (
              <p>Loading...</p>
            ) : bookings.length === 0 ? (
              <p>No bookings</p>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Room</th>
                    <th>Check-In</th>
                    <th>Check-Out</th>
                    <th>Status</th>
                    <th>Amount</th>
                  </tr>
                </thead>

                <tbody>
                  {bookings.map((b, i) => (
                    <tr key={i}>
                      <td>{b.roomsNumber}</td>
                      <td>{b.checkIn}</td>
                      <td>{b.checkOut}</td>

                      <td className={b.bookingStatus?.toLowerCase()}>
                        {b.bookingStatus}
                      </td>

                      <td>₹{b.amount}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {/* TOURS */}
        {activeTab === "tours" && (
          <div className="table-section">
            <h3>My Tours</h3>

            <table>
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Location</th>
                  <th>Price</th>
                  <th>Days</th>
                  <th>Status</th>
                </tr>
              </thead>

              <tbody>
                {tours.length > 0 ? (
                  tours.map((t, i) => (
                    <tr key={i}>
                      <td>{t.title}</td>
                      <td>{t.location}</td>
                      <td>₹{t.price}</td>
                      <td>{t.durationDays}</td>
                      <td>{t.tourStatus}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="5">No tours</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* CREATE TOUR MODAL */}
      {showCreateTour && (
        <div className="modal-overlay">
          <div className="modal-box" ref={modalRef}>
            <button
              className="close-btn"
              onClick={() => setShowCreateTour(false)}
            >
              ✖
            </button>

            <h2>Create Tour</h2>

            <form
              onSubmit={async (e) => {
                e.preventDefault();
                await createTour(tourData);
                alert("Created ✅");
                setShowCreateTour(false);
                fetchTours();
              }}
            >
              <input
                placeholder="Title"
                onChange={(e) =>
                  setTourData({ ...tourData, title: e.target.value })
                }
              />

              <input
                placeholder="Location"
                onChange={(e) =>
                  setTourData({ ...tourData, location: e.target.value })
                }
              />

              <input
                placeholder="Price"
                type="number"
                onChange={(e) =>
                  setTourData({ ...tourData, price: e.target.value })
                }
              />

              <input
                placeholder="Days"
                type="number"
                onChange={(e) =>
                  setTourData({
                    ...tourData,
                    durationDays: e.target.value,
                  })
                }
              />

              <textarea
                placeholder="Description"
                onChange={(e) =>
                  setTourData({
                    ...tourData,
                    description: e.target.value,
                  })
                }
              />

              <button type="submit">Create</button>
            </form>
          </div>
        </div>
      )}

      {/* UPDATE PROFILE MODAL */}
      {showUpdateForm && (
        <div className="modal-overlay">
          <div className="modal-box" ref={modalRef}>
            <button
              className="close-btn"
              onClick={() => setShowUpdateForm(false)}
            >
              ✖
            </button>

            <h2>Update Profile</h2>

            <form
              onSubmit={async (e) => {
                e.preventDefault();
                await updateHotelProfile(profile);
                alert("Updated ✅");
                setShowUpdateForm(false);
              }}
            >
              <input
                value={profile.hotel || ""}
                onChange={(e) =>
                  setProfile({ ...profile, hotel: e.target.value })
                }
              />

              <input
                value={profile.address || ""}
                onChange={(e) =>
                  setProfile({ ...profile, address: e.target.value })
                }
              />

              <input
                value={profile.city || ""}
                onChange={(e) =>
                  setProfile({ ...profile, city: e.target.value })
                }
              />

              <input
                value={profile.price || ""}
                onChange={(e) =>
                  setProfile({ ...profile, price: e.target.value })
                }
              />

              <button type="submit">Update</button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default HotelLoginDashboard;