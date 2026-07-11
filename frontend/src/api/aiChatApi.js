import axios from "axios";

const API_BASE = process.env.REACT_APP_API_URL || `${process.env.REACT_APP_API_URL || "http://localhost:8080"}`;
const CHAT_URL = `${API_BASE}/api/chat`;

export const askAi = async (message) => {
  return axios.post(`${CHAT_URL}/ask`, { message }, {
    headers: {
      "Content-Type": "application/json",
    },
  });
};
