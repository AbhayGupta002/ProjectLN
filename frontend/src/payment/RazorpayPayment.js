import axios from "axios";

const API_BASE = process.env.REACT_APP_API_URL || "http://localhost:8080";

// Helper to dynamically load Razorpay checkout script if needed
export const loadRazorpayScript = () => {
  return new Promise((resolve) => {
    if (window.Razorpay) {
      resolve(true);
      return;
    }
    const script = document.createElement("script");
    script.src = "https://checkout.razorpay.com/v1/checkout.js";
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
};

/**
 * Authoritative, Tamper-Proof Booking Payment Flow.
 * Amount is calculated server-side directly from the database entity to prevent tampering.
 * Explicitly enables UPI QR Code, Google Pay, PhonePe, and UPI Collect.
 */
export const startBookingPayment = async ({
  bookingType,
  bookingId,
  userEmail,
  userName,
  userMobile,
  onSuccess,
  onFailure,
}) => {
  try {
    const loaded = await loadRazorpayScript();
    if (!loaded) {
      throw new Error("Razorpay SDK failed to load. Please check your internet connection.");
    }

    const token = localStorage.getItem("token");
    const authHeaders = token ? { Authorization: `Bearer ${token}` } : {};

    // Step 1: Create Order authoritatively on backend
    const response = await axios.post(
      `${API_BASE}/api/payment/create-booking-order`,
      null,
      {
        params: { bookingType, bookingId },
        headers: authHeaders,
      }
    );

    const order = response.data;
    const finalKey = order.key || process.env.REACT_APP_RAZORPAY_KEY_ID || "rzp_test_51HMACdummyKey";

    // Step 2: Configure Razorpay Checkout with UPI QR & GPay / PhonePe prioritized
    const options = {
      key: finalKey,
      amount: order.amount,
      currency: order.currency || "INR",
      name: "WorldTours Luxury Travel",
      description: `${bookingType.toUpperCase()} Booking #${bookingId}`,
      image: "/assets/logo-badge.png",
      order_id: order.id,

      config: {
        display: {
          blocks: {
            upi: {
              name: "Pay via UPI (PhonePe, GPay, QR Code)",
              instruments: [
                {
                  method: "upi",
                  apps: ["google_pay", "phonepe", "paytm", "bhim"],
                },
              ],
            },
            other: {
              name: "Cards, NetBanking & Wallets",
              instruments: [
                { method: "card" },
                { method: "netbanking" },
                { method: "wallet" },
              ],
            },
          },
          sequence: ["block.upi", "block.other"],
          preferences: {
            show_default_blocks: true,
          },
        },
      },

      prefill: {
        name: userName || localStorage.getItem("userName") || "",
        email: userEmail || localStorage.getItem("email") || "",
        contact: userMobile || localStorage.getItem("mobile") || "",
      },

      notes: {
        bookingType: bookingType.toUpperCase(),
        bookingId: String(bookingId),
      },

      theme: {
        color: "#0284c7",
      },

      handler: async function (paymentResponse) {
        try {
          // Step 3: Verify cryptographic HMAC-SHA256 signature on backend
          const verifyPayload = {
            orderId: paymentResponse.razorpay_order_id,
            paymentId: paymentResponse.razorpay_payment_id,
            signature: paymentResponse.razorpay_signature,
            bookingId: bookingId,
            paymentMethod: "UPI / ONLINE",
          };

          const verifyRes = await axios.post(
            `${API_BASE}/api/payment/verify-booking?bookingType=${bookingType.toUpperCase()}`,
            verifyPayload,
            { headers: authHeaders }
          );

          if (verifyRes.data && verifyRes.data.success) {
            if (onSuccess) {
              onSuccess({
                ...paymentResponse,
                verified: true,
                message: "Payment verified and booking confirmed",
              });
            }
          } else {
            throw new Error(verifyRes.data?.message || "Signature verification failed");
          }
        } catch (verifyErr) {
          console.error("Payment verification error:", verifyErr);
          if (onFailure) {
            onFailure(verifyErr);
          } else {
            alert("Payment signature verification failed. Please contact support.");
          }
        }
      },

      modal: {
        ondismiss: function () {
          if (onFailure) onFailure(new Error("Payment cancelled by user"));
        },
      },
    };

    const paymentObj = new window.Razorpay(options);
    paymentObj.open();
  } catch (err) {
    console.error("Payment initialization error:", err);
    if (onFailure) {
      onFailure(err);
    } else {
      alert(err.response?.data?.error || err.message || "Payment initialization failed.");
    }
  }
};

/**
 * Backward-compatible startPayment helper
 */
export const startPayment = async (amount, onSuccess, onFailure) => {
  try {
    const loaded = await loadRazorpayScript();
    if (!loaded) {
      throw new Error("Razorpay SDK failed to load");
    }

    const token = localStorage.getItem("token");
    const authHeaders = token ? { Authorization: `Bearer ${token}` } : {};

    const response = await axios.post(
      `${API_BASE}/api/payment/createOrder`,
      { amount },
      { headers: authHeaders }
    );

    const order = response.data;
    const finalKey = order.key || process.env.REACT_APP_RAZORPAY_KEY_ID || "rzp_test_51HMACdummyKey";

    const options = {
      key: finalKey,
      amount: order.amount,
      currency: order.currency || "INR",
      name: "WorldTours Luxury Travel",
      description: "Booking Payment",
      image: "/assets/logo-badge.png",
      order_id: order.id,
      handler: async function (resp) {
        if (onSuccess) onSuccess(resp);
      },
      prefill: {
        name: localStorage.getItem("userName") || "",
        email: localStorage.getItem("email") || "",
      },
      theme: {
        color: "#0284c7",
      },
    };

    const paymentObj = new window.Razorpay(options);
    paymentObj.open();
  } catch (err) {
    console.error("Payment error:", err);
    if (onFailure) onFailure(err);
    else alert("Payment failed. Please try again.");
  }
};
