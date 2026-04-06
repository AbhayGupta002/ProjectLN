import React, { useState } from "react";
import { addComplaint } from "../api/complaintApi";
import "../styles/Complaint.css";

const ComplaintForm = () => {
  const [userId, setUserId] = useState("");
  const [bookingId, setBookingId] = useState("");
  const [message, setMessage] = useState("");
  const [success, setSuccess] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();

    const reqData = {
      userId,
      bookingId: bookingId || null,
      message
    };

    try {
      await addComplaint(reqData);
      setSuccess("Complaint submitted successfully!");
      setUserId("");
      setBookingId("");
      setMessage("");
    } catch (error) {
      console.error(error);
      setSuccess("Failed to submit complaint.");
    }
  };

  return (
    <div className="complaint-container">
      <form className="complaint-box" onSubmit={handleSubmit}>
        <h2>Submit a Complaint</h2>

        <input
          type="text"
          placeholder="User ID"
          value={userId}
          onChange={(e) => setUserId(e.target.value)}
          required
        />

        <input
          type="text"
          placeholder="Booking ID (optional)"
          value={bookingId}
          onChange={(e) => setBookingId(e.target.value)}
        />

        <textarea
          placeholder="Write your complaint"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          required
        />

        <button type="submit">Submit Complaint</button>

        {success && <p className="success">{success}</p>}
      </form>
    </div>
  );
};

export default ComplaintForm;
