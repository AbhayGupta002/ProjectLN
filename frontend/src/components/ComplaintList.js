import React, { useEffect, useState } from "react";
import { getUserComplaints } from "../api/complaintApi";
import "../styles/Complaint.css";

const ComplaintList = ({ userId }) => {
  const [complaints, setComplaints] = useState([]);

  useEffect(() => {
    loadComplaints();
  }, []);

  const loadComplaints = async () => {
    try {
      const response = await getUserComplaints(userId);
      setComplaints(response.data);
    } catch (error) {
      console.error("Error fetching complaints:", error);
    }
  };

  return (
    <div className="complaint-list-container">
      <h2>Your Complaints</h2>

      {complaints.length === 0 ? (
        <p>No complaints found.</p>
      ) : (
        complaints.map((c) => (
          <div key={c.id} className="complaint-card">
            <h3>Complaint #{c.id}</h3>
            <p><strong>Message:</strong> {c.message}</p>
            <p><strong>Booking ID:</strong> {c.bookingId ?? "None"}</p>
            <p><strong>Date:</strong> {c.createdAt}</p>
          </div>
        ))
      )}
    </div>
  );
};

export default ComplaintList;
