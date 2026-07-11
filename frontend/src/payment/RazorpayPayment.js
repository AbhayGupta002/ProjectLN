import axios from "axios";

const API_BASE = process.env.REACT_APP_API_URL || `${process.env.REACT_APP_API_URL || "http://localhost:8080"}`;

export const startPayment = async (amount, onSuccess, onFailure) => {
  try {
    // Step 1: Create order from backend
    const response = await axios.post(`${API_BASE}/api/payment/createOrder`, {
      amount: amount,
    });

    const order = response.data;

    // Step 2: Razorpay Checkout Options
    const options = {
      key: order.key || process.env.REACT_APP_RAZORPAY_KEY_ID || "rzp_test_XXXXXXX",
      amount: order.amount,
      currency: order.currency || "INR",
      name: "ProjectLN Travel Booking",
      description: "Booking Payment",
      order_id: order.id,

      handler: async function (response) {
        if (onSuccess) {
          onSuccess({
            razorpay_payment_id: response.razorpay_payment_id,
            razorpay_order_id: response.razorpay_order_id,
            razorpay_signature: response.razorpay_signature,
          });
        }
      },

      prefill: {
        name: localStorage.getItem("userName") || "",
        email: localStorage.getItem("userEmail") || "",
      },

      theme: {
        color: "#2563eb",
      },
    };

    const paymentObj = new window.Razorpay(options);
    paymentObj.open();

  } catch (err) {
    console.error("Payment error:", err);
    if (onFailure) {
      onFailure(err);
    } else {
      alert("Payment failed, please try again.");
    }
  }
};
