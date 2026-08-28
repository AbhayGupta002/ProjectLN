import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import { getInitialTheme, applyTheme } from "./utils/theme";

// Initialize persistent theme (75% Dark, 50% Light, or 25% Light)
applyTheme(getInitialTheme());

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
