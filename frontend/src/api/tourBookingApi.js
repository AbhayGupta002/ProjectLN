import axios from "axios";

const API_URL = "http://localhost:8080/api/tour-booking";

export const createTourBooking = async (bookingData) => {
  const token = localStorage.getItem("token");

  return axios.post(`${API_URL}/create`, bookingData, {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });
};

export const getUserTourBookings = async (userId) => {
  const token = localStorage.getItem("token");

  return axios.get(`${API_URL}/user/${userId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};

export const cancelTourBooking = async (bookingId) => {
  const token = localStorage.getItem("token");

  return axios.put(`${API_URL}/cancel/${bookingId}`, {}, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};
