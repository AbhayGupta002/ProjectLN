import axios from "axios";

const API = axios.create({
  baseURL: "http://localhost:8080/api"
});

export default API;


// ---------- AUTH ----------

export const registerUser = async (userData) => {
  const response = await API.post("/auth/register", userData);
  return response.data;
};

export const loginUser = async (credentials) => {
  const response = await API.post("/auth/login", credentials);
  return response.data;
};

export const registerHotel = async (hotelData) => {
  const response = await API.post("/auth/hotelregister", hotelData);
  return response.data;
};

export const loginHotel = async (hotelLogin) => {
  const response = await API.post("/auth/hotellogin", hotelLogin);
  return response.data;
};


// ---------- AI CHAT ----------

export const askAI = async (msg) => {
  const response = await API.post("/chat/ask", {
    message: msg
  });
  return response.data;
};