import axios from "axios";

const BASE_URL = "http://localhost:8080/api/bookings";

const getAuthHeader = () => {
  const token =
    localStorage.getItem("hotelToken") ||
    localStorage.getItem("token");

  return {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };
};

// ✅ Get bookings by hotel
export const getHotelBookings = async () => {
  const token =
    localStorage.getItem("hotelToken") ||
    localStorage.getItem("token");

  return axios.get(`${BASE_URL}/getallbookingbyhotel`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};
