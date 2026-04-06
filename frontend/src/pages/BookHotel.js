import React from "react";
import { startPayment } from "../payment/RazorpayPayment";

function BookHotel() {
  return (
    <div>
      <h1>Book Hotel</h1>
      <button onClick={() => startPayment(500)}>Pay ₹500</button>
    </div>
  );
}

export default BookHotel;
