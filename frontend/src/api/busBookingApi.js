import axios from "axios";

const API_BASE = process.env.REACT_APP_API_URL || `${process.env.REACT_APP_API_URL || "http://localhost:8080"}`;
const BUS_URL = `${API_BASE}/api/bus`;
const BOOKING_URL = `${API_BASE}/api/bus-bookings`;

export const getAllBuses = async () => {
  return axios.get(`${BUS_URL}/all`);
};

export const searchBuses = async (source, destination) => {
  return axios.get(`${BUS_URL}/search`, {
    params: { source, destination }
  });
};

export const bookBus = async (bookingData) => {
  const token = localStorage.getItem("token");
  return axios.post(`${BOOKING_URL}/book`, bookingData, {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });
};

export const getUserBusBookings = async (userId) => {
  const token = localStorage.getItem("token");
  return axios.get(`${BOOKING_URL}/user/${userId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};

export const cancelBusBooking = async (bookingId) => {
  const token = localStorage.getItem("token");
  return axios.put(`${BOOKING_URL}/cancel/${bookingId}`, {}, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};
