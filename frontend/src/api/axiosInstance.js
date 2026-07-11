import axios from "axios";

const axiosInstance = axios.create({
  baseURL: `${process.env.REACT_APP_API_URL || "http://localhost:8080"}/api/admin`, // ✅ base
});

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem("adminToken");

  if (token) {
    config.headers["Authorization"] = `Bearer ${token}`;
  }

  return config;
});

export default axiosInstance;