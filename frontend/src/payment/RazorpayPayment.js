import axios from "axios";

export const startPayment = async (amount) => {
  try {
    // Step 1: Create order from backend
    const { data } = await axios.post("http://localhost:8080/api/payment/createOrder", {
      amount: amount,
    });

    const order = JSON.parse(data);

    // Step 2: Razorpay Checkout Options
    const options = {
      key: "rzp_test_XXXXXXX", // Your Key ID
      amount: order.amount,
      currency: "INR",
      name: "Hotel Booking App",
      description: "Room booking payment",
      order_id: order.id,

      handler: async function (response) {
        alert("Payment Successful!");

        console.log("Payment ID:", response.razorpay_payment_id);
        console.log("Order ID:", response.razorpay_order_id);
        console.log("Signature:", response.razorpay_signature);

        // TODO: Call backend to confirm booking
      },

      theme: {
        color: "#3399cc",
      },
    };

    const paymentObj = new window.Razorpay(options);
    paymentObj.open();

  } catch (err) {
    console.error("Payment error:", err);
    alert("Payment failed, try again.");
  }
};
