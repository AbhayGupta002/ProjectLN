import axios from "axios";

import axiosInstance from "./axiosInstance";
const BASE_URL = "http://localhost:8080/api/admin";

export const adminLogin = async (credentials) => {
  try {
    const response = await axios.post(`${BASE_URL}/login`, credentials);
    return response.data; // { data: token, error: null }
  } catch (error) {
    return error.response?.data || {
      error: { message: "Login failed" },
    };
  }
};


// src/api/adminPanelApi.js

// ---------------- AUTH ----------------
//export const adminLoginApi = async (data) => {
//  const res = await axiosInstance.post("/login", data);
//  return res.data; // { data: token }
//};

// ---------------- HOTELS ----------------
export const getAllHotels = async () => {
  const res = await axiosInstance.get("/hotels");
  return res.data;
};

export const getActiveHotels = async () => {
  const res = await axiosInstance.get("/active-hotels");
  return res.data;
};

export const getInactiveHotels = async () => {
  const res = await axiosInstance.get("/inactive-hotels");
  return res.data;
};

// ---------------- USERS ----------------
export const getAllUsers = async () => {
  const res = await axiosInstance.get("/all-users");
  return res.data;
};

// ---------------- ACTIONS ----------------
export const suspendHotelApi = async (id) => {
  const res = await axiosInstance.patch(`/suspend-hotel/${id}`);
  return res.data;
};

export const suspendUserApi = async (id) => {
  const res = await axiosInstance.patch(`/suspend-user/${id}`);
  return res.data;
};

// ---------------- SEARCH ----------------
export const searchHotelsApi = async (location) => {
  const res = await axiosInstance.get(`/search?location=${location}`);
  return res.data;
};