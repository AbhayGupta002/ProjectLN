import axios from "axios";

const API_BASE = process.env.REACT_APP_API_URL || `${process.env.REACT_APP_API_URL || "http://localhost:8080"}`;
const FLIGHT_URL = `${API_BASE}/api/flights`;
const BOOKING_URL = `${API_BASE}/api/flight-bookings`;

export const getAllFlights = async () => {
  return axios.get(`${FLIGHT_URL}/all`);
};

export const searchFlights = async (source, destination, date, query) => {
  return axios.get(`${FLIGHT_URL}/search`, {
    params: { source, destination, date, query }
  });
};

export const getRapidApiStatus = async () => {
  return axios.get(`${API_BASE}/api/rapidapi/status`);
};

export const testRapidApiKey = async (key) => {
  return axios.post(`${API_BASE}/api/rapidapi/test-key`, { key });
};

export const bookFlight = async (bookingData) => {
  const token = localStorage.getItem("token");
  return axios.post(`${BOOKING_URL}/book`, bookingData, {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });
};

export const getUserFlightBookings = async (userId) => {
  const token = localStorage.getItem("token");
  return axios.get(`${BOOKING_URL}/user/${userId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};

export const cancelFlightBooking = async (bookingId) => {
  const token = localStorage.getItem("token");
  return axios.put(`${BOOKING_URL}/cancel/${bookingId}`, {}, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};
