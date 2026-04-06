import axios from "axios";
const API = "http://localhost:8080/complaint";

export const addComplaint = (data) => {
    return axios.post(`${API}/add`, data);
};

export const getUserComplaints = (userId) => {
    return axios.get(`${API}/user/${userId}`);
};