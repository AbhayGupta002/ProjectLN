import axios from "axios";

const API_URL = `${process.env.REACT_APP_API_URL || "http://localhost:8080"}/api/auth`;

export const sendResetLink = async (email) => {
  try {
    const response = await axios.post(`${API_URL}/forgot-password`, { email: email.trim() });
    return response.data;
  } catch (err) {
    const message =
      err.response?.data?.message ||
      (typeof err.response?.data === "string" ? err.response.data : null) ||
      "Incorrect Details";
    throw new Error(message);
  }
};
