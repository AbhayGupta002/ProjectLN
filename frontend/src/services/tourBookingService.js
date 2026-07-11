import axios from "axios";

const API_URL = `${process.env.REACT_APP_API_URL || "http://localhost:8080"}/api/tour-booking`;

// Create booking
export const createBooking = async (bookingData, token) => {
  return await axios.post(`${API_URL}/create`, bookingData, {
    headers: { Authorization: `Bearer ${token}` }
  });
};

// Get all bookings for a user
export const getUserBookings = async (userId, token) => {
  return await axios.get(`${API_URL}/user/${userId}`, {
    headers: { Authorization: `Bearer ${token}` }
  });
};

// Cancel booking
export const cancelBooking = async (id, token) => {
  return await axios.put(`${API_URL}/cancel/${id}`, {}, {
    headers: { Authorization: `Bearer ${token}` }
  });
};
