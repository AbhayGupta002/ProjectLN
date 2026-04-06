import axios from "axios";
const DASHBOARD_URL = "http://localhost:8080/api/dashboard";

const API = axios.create({
  baseURL: "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
});

// ✅ GET PROFILE
export const getProfile = async () => {
  try {
    const token = localStorage.getItem("token");

    const res = await API.get("/user/profile", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    return res.data;
  } catch (error) {
    return {
      error: error.response?.data || { message: "Failed to load profile" },
    };
  }
};

// ✅ UPDATE PROFILE
export const updateProfile = async (token, data) => {
  try {
    const res = await API.put("/user/update-profile", data, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    return res.data;
  } catch (error) {
    return {
      error: error.response?.data || { message: "Update failed" },
    };
  }
};

//get all active hotels
export const getHotels = async (token) => {
  try {
    const res = await API.get("/dashboard/get-active-hotel", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    return res.data;
  } catch (error) {
    return {
      error: error.response?.data || { message: "Failed to fetch hotels" },
    };
  }
};