import React, { useState, useEffect } from "react";
import axiosInstance from "../api/axiosInstance";
import { searchByLocation, searchTourByDays } from "../api/publicApi";
import "../styles/AdminDashboard.css";
import { useNavigate } from "react-router-dom";
import {
  suspendHotelApi,
  suspendUserApi
} from "../api/adminPanelApi";

function AdminPanel() {
const [showHotelModal, setShowHotelModal] = useState(false);
const [selectedHotelId, setSelectedHotelId] = useState(null);
const [successMessage, setSuccessMessage] = useState("");
const [showModal, setShowModal] = useState(false);
const [selectedUserId, setSelectedUserId] = useState(null);
  const [data, setData] = useState([]);
  const [view, setView] = useState("hotels");
  const [searchLocation, setSearchLocation] = useState("");
  const [days, setDays] = useState("");
  const [loading, setLoading] = useState(false);
  const [tours, setTours] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchData("hotels");
  }, []);

const confirmSuspendHotel = async () => {
  try {
    console.log("Suspending hotel:", selectedHotelId);

    const res = await suspendHotelApi(selectedHotelId);

    setSuccessMessage(res.data || "Hotel suspended successfully");

    fetchData(view);
    setShowHotelModal(false);
    setSelectedHotelId(null);

  } catch (err) {
    console.error(err.response || err);
    alert("Failed to suspend hotel");
  }
};


  //confirm suspend
//  const confirmSuspendUser = async () => {
//    try {
//      console.log("Suspending user:", selectedUserId);
//
//      const res = await suspendUserApi(selectedUserId);
//
//      console.log("Response:", res);
//
//      fetchData(view);
//      setShowModal(false);
//
//    } catch (err) {
//      console.error("ERROR:", err.response || err);
//    }
//  };
const confirmSuspendUser = async () => {
  try {
    const res = await suspendUserApi(selectedUserId);

    setSuccessMessage(res.data || "User suspended successfully");

    fetchData(view);
    setShowModal(false);

  } catch (err) {
    console.error(err);
    alert("Failed to suspend user");
  }
};

  const fetchData = async (type) => {
    try {
      let res;

      switch (type) {
        case "hotels":
          res = await axiosInstance.get("/hotels");
          break;
        case "active":
          res = await axiosInstance.get("/hotels/active");
          break;
        case "inactive":
          res = await axiosInstance.get("/hotels/inactive");
          break;
        case "users":
          res = await axiosInstance.get("/users");
          break;
        case "activities":
          res = await axiosInstance.get("/prompts");
          break;
        default:
          return;
      }

      const responseData = res.data?.data || res.data;
      setData(responseData);
      setView(type);

    } catch (err) {
      console.error("FETCH ERROR:", err);
      setData([]);
      setView(type);
    }
  };

  const handleSearch = async () => {
    if (!searchLocation && !days) {
      alert("Enter location OR days");
      return;
    }

    try {
      setLoading(true);
      let res;

      if (searchLocation) {
        res = await searchByLocation(searchLocation);
      } else {
        res = await searchTourByDays(days);
      }

      if (res.error) {
        alert(res.error.message);
        setTours([]);
      } else {
        setTours(res.data);
      }

    } catch (err) {
      console.error(err);
      alert("Error fetching tours");
    } finally {
      setLoading(false);
    }
  };

  const suspendHotel = async (id) => {
    try {
      await suspendHotelApi(id);
      fetchData(view);
    } catch (err) {
      console.error(err);
    }
  };

  const logout = () => {
    localStorage.removeItem("adminToken");
    localStorage.removeItem("role");
    localStorage.removeItem("email");
    navigate("/admin-login");
  };

  return (
    <div className="admin-dashboard">

      {/* ===== SIDEBAR ===== */}
      <div className="admin-sidebar">
        <h2>Admin Panel</h2>

        <button onClick={() => fetchData("hotels")}>All Hotels</button>
        <button onClick={() => fetchData("active")}>Active Hotels</button>
        <button onClick={() => fetchData("inactive")}>Inactive Hotels</button>
        <button onClick={() => fetchData("users")}>Users</button>
        <button onClick={() => fetchData("activities")}>Activities Log</button>

        <button onClick={logout} className="admin-logout">
          Logout
        </button>
      </div>

      {/* ===== MAIN ===== */}
      <div className="admin-main">

        <h2>{view.toUpperCase()}</h2>

        {/* ===== SEARCH ===== */}
        <div className="admin-search-box">
          <input
            type="text"
            placeholder="Enter location (Goa, Delhi...)"
            value={searchLocation}
            onChange={(e) => {
              setSearchLocation(e.target.value);
              setDays("");
            }}
          />

          <input
            type="number"
            placeholder="Enter days (e.g. 5)"
            value={days}
            onChange={(e) => {
              setDays(e.target.value);
              setSearchLocation("");
            }}
            min="1"
          />

          <button onClick={handleSearch}>
            {loading ? "Searching..." : "Search"}
          </button>
        </div>

        {/* ===== TOUR RESULTS ===== */}
        {tours.length > 0 && (
          <>
            <h3>Search Results</h3>

            <div className="admin-card-container">
              {tours.map((t, i) => (
                <div key={i} className="admin-card">
                  <img src={t.imageUrl} alt={t.title} />
                  <h3>{t.title}</h3>
                  <p>{t.location}</p>
                  <p>{t.durationDays} Days</p>
                  <p>₹{t.price}</p>
                </div>
              ))}
            </div>
          </>
        )}

        {successMessage && (
          <div className="admin-modal-overlay">
            <div className="admin-modal-box">
              <h3>✅ Success</h3>
              <p>{successMessage}</p>

              <button
                className="admin-btn"
                onClick={() => setSuccessMessage("")}
              >
                OK
              </button>
            </div>
          </div>
        )}

        {showHotelModal && (
          <div className="admin-modal-overlay">
            <div className="admin-modal-box">
              <h3>Are you sure?</h3>
              <p>Do you want to suspend this hotel?</p>

              <div className="admin-modal-actions">
                <button
                  className="admin-btn reject-btn"
                  onClick={confirmSuspendHotel}
                >
                  Suspend
                </button>

                <button
                  className="admin-btn"
                  onClick={() => setShowHotelModal(false)}
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        )}

        {/*confirm suspend popup*/}
        {showModal && (
          <div className="admin-modal-overlay">
            <div className="admin-modal-box">
              <h3>Are you sure?</h3>
              <p>Do you want to suspend this user?</p>

              <div className="admin-modal-actions">
                <button
                  className="admin-btn reject-btn"
                  onClick={confirmSuspendUser}
                >
                  Suspend
                </button>

                <button
                  className="admin-btn"
                  onClick={() => setShowModal(false)}
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        )}

        {/* ===== TABLE ===== */}
        <div className="admin-table-container">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email / Location</th>
                <th>Action</th>
              </tr>
            </thead>

            <tbody>
              {data && data.length > 0 ? (
                view === "activities" ? (
                  data.map((promptText, index) => (
                    <tr key={index}>
                      <td>{index + 1}</td>
                      <td>User AI Assistant Query</td>
                      <td>{promptText}</td>
                      <td><span style={{ color: "#2563eb", fontWeight: "bold" }}>LOGGED</span></td>
                    </tr>
                  ))
                ) : (
                  data.map((item) => (
                    <tr key={item.id}>
                      <td>{item.id}</td>
                      <td>{item.name || item.hotel}</td>
                      <td>{item.email || item.location}</td>
                      <td>
                        {view === "users" ? (
                          <button
                            className="admin-btn reject-btn"
                           onClick={() => {
                               console.log("Clicked ID:", item.id); // debug
                               setSelectedUserId(item.id);
                               setShowModal(true);
                             }}
                           >
                             Suspend User
                           </button>
                        ) : (
                          <button
                          className="admin-btn reject-btn"
                            onClick={() => {
                               console.log("Hotel ID:", item.id); // debug
                               setSelectedHotelId(item.id);
                               setShowHotelModal(true);
                             }}
                           >
                            Suspend Hotel
                          </button>
                        )}
                      </td>
                    </tr>
                  ))
                )
              ) : (
                <tr>
                  <td colSpan="4">No Data Found</td>
                </tr>
              )}
            </tbody>

          </table>
        </div>

      </div>
    </div>
  );
}

export default AdminPanel;