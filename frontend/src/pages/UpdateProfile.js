//import React, { useEffect, useState } from "react";
//import { getProfile, updateProfile } from "../api/userDashboardApi";
//
//function UpdateProfile() {
//  const [formData, setFormData] = useState({
//    name: "",
//    mobile: "",
//    city: "",
//  });
//
//  useEffect(() => {
//    loadUser();
//  }, []);
//
//  const loadUser = async () => {
//    const res = await getProfile();
//
//    if (res.error) {
//      alert(res.error.message);
//      return;
//    }
//
//    setFormData(res.data);
//  };
//
//  const handleChange = (e) => {
//    setFormData({ ...formData, [e.target.name]: e.target.value });
//  };
//
//  const handleSubmit = async (e) => {
//    e.preventDefault();
//
//    const token = localStorage.getItem("token");
//    const res = await updateProfile(token, formData);
//
//
//    if (res.error) {
//      alert(res.error.message);
//    } else {
//      alert("Profile updated successfully!");
//      window.location.href = "/dashboard";
//    }
//  };
//
//  return (
//    <div className="profile-container">
//      <h2>Update Profile</h2>
//
//      <form onSubmit={handleSubmit} className="profile-form">
//        <input
//          type="text"
//          name="name"
//          placeholder="Full Name"
//          value={formData.name}
//          onChange={handleChange}
//        />
//
//        <input
//          type="text"
//          name="mobile"
//          placeholder="Mobile Number"
//          value={formData.mobile}
//          onChange={handleChange}
//        />
//
//        <input
//          type="text"
//          name="city"
//          placeholder="City"
//          value={formData.city}
//          onChange={handleChange}
//        />
//
//        <button type="submit" className="btn-save">Save</button>
//      </form>
//    </div>
//  );
//}
//
//export default UpdateProfile;
