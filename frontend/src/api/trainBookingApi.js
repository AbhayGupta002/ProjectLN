import axios from "axios";

const API_BASE = process.env.REACT_APP_API_URL || `${process.env.REACT_APP_API_URL || "http://localhost:8080"}`;
const TRAIN_URL = `${API_BASE}/api/trains`;
const BOOKING_URL = `${API_BASE}/api/train-bookings`;

export const getAllTrains = async () => {
  return axios.get(`${TRAIN_URL}/all`);
};

export const searchTrains = async (source, destination) => {
  return axios.get(`${TRAIN_URL}/search`, {
    params: { source, destination }
  });
};

export const bookTrain = async (bookingData) => {
  const token = localStorage.getItem("token");
  return axios.post(`${BOOKING_URL}/book`, bookingData, {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });
};

export const getUserTrainBookings = async (userId) => {
  const token = localStorage.getItem("token");
  return axios.get(`${BOOKING_URL}/user/${userId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};

export const cancelTrainBooking = async (bookingId) => {
  const token = localStorage.getItem("token");
  return axios.put(`${BOOKING_URL}/cancel/${bookingId}`, {}, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};
