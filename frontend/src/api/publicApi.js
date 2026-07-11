import axios from "axios";

const BASE_URL = `${process.env.REACT_APP_API_URL || "http://localhost:8080"}/api/public`;

// 🔥 Location API
export const searchByLocation = async (location, sort = "asc") => {
  const res = await axios.get(`${BASE_URL}/location`, {
    params: { location, sort }
  });
  return res.data;
};

// 🔥 Days API
export const searchTourByDays = async (days) => {
  const res = await axios.get(`${BASE_URL}/days`, {
    params: { days }
  });
  return res.data;
};

// src/api/hotelApi.js
export const getTopHotels = async () => {
  try {
    const response = await axios.get(`${BASE_URL}/tophotels`);
    return response.data;
  } catch (error) {
    console.error("Error fetching top hotels:", error);
    throw error;
  }
};