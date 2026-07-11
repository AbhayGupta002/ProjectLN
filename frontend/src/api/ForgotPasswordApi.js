import axios from "axios";

const API_URL = `${process.env.REACT_APP_API_URL || "http://localhost:8080"}/api/auth`;

export const sendResetLink = async (email) => {
  try {
    const response = await axios.post(`${API_URL}/forgot-password`, { email });
    return response.data;
  } catch (err) {
    try {
      const response = await axios.post(`${API_URL}/forgot-password`, null, {
        params: { email },
      });
      return response.data;
    } catch (err2) {
      const message =
        err2.response?.data ||
        err.response?.data ||
        err2.message ||
        "Failed to send reset link";
      throw new Error(message);
    }
  }
};
