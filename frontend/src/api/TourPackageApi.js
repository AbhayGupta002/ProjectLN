// src/api/hotelTourApi.js
import axios from "axios";

const API_URL = "http://localhost:8080/api/tours";

// Create a new tour package (Hotel side)
export const createTour = async (data) => {
  const token =
    localStorage.getItem("hotelToken") ||
    localStorage.getItem("token");

  return axios.post(
    "http://localhost:8080/api/tours/create",
    data,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
};




// Get single tour package by ID
export const getTourPackageById = async (id) => {
  const token = localStorage.getItem("hotelToken") || localStorage.getItem("token");

  return axios.get(`${API_URL}/${id}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};

// Update a tour package
export const updateTourPackage = async (id, tourData) => {
  const token = localStorage.getItem("hotelToken") || localStorage.getItem("token");

  return axios.put(`${API_URL}/update/${id}`, tourData, {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });
};

// Delete a tour package
export const deleteTourPackage = async (id) => {
  const token = localStorage.getItem("hotelToken") || localStorage.getItem("token");

  return axios.delete(`${API_URL}/delete/${id}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};

// ✅ GET ALL TOURS BY HOTEL EMAIL
// ✅ HEADER
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

// ✅ GET ALL TOURS
export const getAllTourPackages = async () => {
  const token =
    localStorage.getItem("hotelToken") ||
    localStorage.getItem("token");

  return axios.get("http://localhost:8080/api/tours/all", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};