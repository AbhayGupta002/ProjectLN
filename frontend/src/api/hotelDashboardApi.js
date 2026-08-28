import axios from "axios";

const BASE_URL = `${process.env.REACT_APP_API_URL || "http://localhost:8080"}/api/hotellogindashboard`;

//// Get token from localStorage
//const getAuthHeader = () => {
//  const token = localStorage.getItem("token");
//  return {
//    headers: {
//      Authorization: `Bearer ${token}`,
//    },
//  };
//};

// ✅ Update Profile API
// ✅ ADD HERE (TOP LEVEL)
const getAuthHeader = () => {
  const token = localStorage.getItem("hotelToken");

  if (!token) {
    throw new Error("No hotel token found");
  }

  return {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };
};

// Get Hotel Profile
export const getHotelProfile = async () => {
  return axios.get(`${BASE_URL}/hotelprofile`, getAuthHeader());
};

// Update Hotel Profile
export const updateHotelProfile = async (data) => {
  return axios.put(
    `${BASE_URL}/update-profile`,
    data,
    getAuthHeader()
  );
};

export const getDashboardStats = async () => {
  const token = localStorage.getItem("hotelToken");

  return axios.get(
    `${process.env.REACT_APP_API_URL || "http://localhost:8080"}/api/hotellogindashboard/dashboard-stats`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
};

