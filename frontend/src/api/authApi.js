import axios from "axios";

const API_URL = "http://localhost:8080/api/auth";
const DASHBOARD_URL = "http://localhost:8080/api/dashboard";
const HOTEL_DASHBOARD_URL = "http://localhost:8080/api/hotellogindashboard";


// ---------------- AUTH -----------------

export const registerUser = async (userData) => {
  const response = await axios.post(`${API_URL}/register`, userData);
  return response.data;
};

export const loginUser = async (credentials) => {
  const response = await axios.post(`${API_URL}/login`, credentials);
  return response.data;
};

export const registerHotel = async (hotelData) => {
  const response = await axios.post(`${API_URL}/hotelregister`, hotelData);
  return response.data;
};

export const loginHotel = async (hotellogin) => {
  const response = await axios.post(`${API_URL}/hotellogin`, hotellogin);
  return response.data;
};


// ---------------- HOTEL DASHBOARD -----------------

export const fetchHotelProfile = async (hotelToken) => {
  const response = await axios.get(
    `${HOTEL_DASHBOARD_URL}/hotelprofile`,
     {
    headers: { Authorization: `Bearer ${hotelToken}` },
  });
  return response.data;
};


// ---------------- USER PROFILE -----------------

//export const updateProfile = async (token, updatedData) => {
//  const response = await axios.put(
//    `${DASHBOARD_URL}/update-profile`,
//    updatedData,
//    {
//      headers: { Authorization: `Bearer ${token}` },
//    }
//  );
//  return response.data;
//};
//
//
//
//export const getAllHotel = async (token) => {
//  const response = await axios.get(
//    `${DASHBOARD_URL}/getAllHotel`,
//    {
//      headers: {
//        Authorization: `Bearer ${token}`
//      }
//    }
//  );
//  return response.data;
//};

export const askAI = async (msg) => {
  return await axios.post("http://localhost:8080/api/chat/ask", {
    message: msg,
  });
};
