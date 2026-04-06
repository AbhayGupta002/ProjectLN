import axios from "axios";

const BASE_URL = "http://localhost:8080/api/otp";

// ✅ VERIFY OTP
export const verifyOtp = async (email, otp) => {
  return axios.post(`${BASE_URL}/verify`, null, {
    params: { email, otp }
  });
};