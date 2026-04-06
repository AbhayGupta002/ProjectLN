import "../css/Home.css";
import API from "../services/api";
import { useState, useRef, useEffect } from "react";

function Home() {

  const [darkMode, setDarkMode] = useState(false);
  const [prompt, setPrompt] = useState("");
  const [messages, setMessages] = useState([]);

  const [showLogin, setShowLogin] = useState(false);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [role, setRole] = useState("user");
  const [isRegister, setIsRegister] = useState(false);

  const [name, setName] = useState("");
  const [mobile, setMobile] = useState("");

  const[number ,setNumber] = useState("");
  const[city, setCity] = useState("");
  const[address, setAddress] = useState("");
  const[price, setPrice] = useState("");

  const chatEndRef = useRef(null);

  const toggleMode = () => {
    setDarkMode(!darkMode);
  };

  // Scroll chat
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  // Logout on refresh
  useEffect(() => {

    localStorage.removeItem("token");
    localStorage.removeItem("hotelToken");
    localStorage.removeItem("adminToken");

    setShowLogin(true);

  }, []);

  // Reopen popup every 15 sec if not logged in
  useEffect(() => {

    const interval = setInterval(() => {

      const token =
        localStorage.getItem("token") ||
        localStorage.getItem("hotelToken") ||
        localStorage.getItem("adminToken");

      if (!token) setShowLogin(true);

    }, 15000);

    return () => clearInterval(interval);

  }, []);

  const closePopup = () => {
    setShowLogin(false);
  };

  // LOGIN
  const handleLogin = async () => {

    try {

      let endpoint = "";

      if (role === "user") endpoint = "/auth/login";
      if (role === "hotel") endpoint = "/auth/hotellogin";
      if (role === "admin") endpoint = "/auth/adminlogin";

      const res = await API.post(endpoint, { email, password });

      const token = res.data;

      if (!token) {
        alert("Token not received");
        return;
      }

      if (role === "hotel") localStorage.setItem("hotelToken", token);
      else if (role === "admin") localStorage.setItem("adminToken", token);
      else localStorage.setItem("token", token);

      alert("✅ Login Successful");

      setShowLogin(false);

    } catch (err) {

      alert(
        err.response?.data?.error?.message ||
        "Invalid credentials"
      );

    }

  };

  // REGISTER
  const handleRegister = async () => {

    try {

      if (role === "user") {

        await API.post("/auth/register", {
          name,
          email,
          mobile,
          password
        });

        alert("✅ User registered");

      }

      if (role === "hotel") {

        await API.post("/auth/hotelregister", {
          hotel: name,
          email,
          password
        });

        alert("🏨 Hotel registered");

      }

      setIsRegister(false);

    } catch (err) {

      alert(
        err.response?.data?.error?.message ||
        "Registration failed"
      );

    }

  };

  // LOGOUT
  const handleLogout = () => {

    localStorage.removeItem("token");
    localStorage.removeItem("hotelToken");
    localStorage.removeItem("adminToken");

    setMessages([]);
    setShowLogin(true);

    alert("Logged out");

  };

  // SEND AI PROMPT
  const sendPrompt = async () => {

    if (!prompt.trim()) return;

    const userText = prompt;
    setPrompt("");

    const userMessage = { role: "user", text: userText };

    setMessages(prev => [...prev, userMessage]);

    try {

      const response = await API.post("/ai/prompt", {
        prompt: userText
      });

      const aiMessage = {
        role: "ai",
        text: typeof response.data === "string"
          ? response.data
          : JSON.stringify(response.data)
      };

      setMessages(prev => [...prev, aiMessage]);

    } catch (error) {

      setMessages(prev => [
        ...prev,
        { role: "ai", text: "Something went wrong" }
      ]);

    }

  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter") sendPrompt();
  };

  return (
    <>

      {/* LOGIN / REGISTER POPUP */}

      {showLogin && (

        <div className="login-overlay">


          <div className="login-modal">
              <span
                              style={{
                                color: "blue",
                                cursor: "pointer",
                                marginLeft: "100px",
                                position:"fixed"
                              }}
                              onClick={() => setIsRegister(!isRegister)}
                            >
                              {isRegister ? "Login" : "Register"}
                            </span>

            <button className="close-btn" onClick={closePopup}>❌</button>

            <h2>{isRegister ? "Register" : "Login"}</h2>

            <select
              value={role}
              onChange={(e) => setRole(e.target.value)}
            >
              <option value="user">User</option>
              <option value="hotel">Hotel</option>
              <option value="admin">Admin</option>
            </select>

            {isRegister && (
              <input
                type="text"
                placeholder={role === "hotel" ? "Hotel Name" : "Full Name"}
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            )}

            {isRegister && role === "user" && (
              <input
                type="text"
                placeholder="Mobile"
                value={mobile}
                onChange={(e) => setMobile(e.target.value)}
              />
            )}

{isRegister && role === "hotel" && (
              <input
                type="text"
                placeholder="MobileNumber"
                value={mobile}
                onChange={(e) => setMobile(e.target.value)}
              />
            )}
        {isRegister && role === "hotel" && (
              <input
                type="text"
                placeholder="city"
                value={city}
                onChange={(e) => setCity(e.target.value)}
              />
            )}
        {isRegister && role === "address" && (
              <input
                type="text"
                placeholder="address"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
              />
            )}
        {isRegister && role === "price" && (
              <input
                type="number"
                placeholder="price"
                value={price}
                onChange={(e) => setPrice(e.target.value)}
              />
            )}
        {isRegister && role === "price" && (
              <input
                type="number"
                placeholder="roomAvl"
                value={roomAvl}
                onChange={(e) => setRoomAvl(e.target.value)}
              />
            )}







            <input
              type="email"
              placeholder="Email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />

            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />

            {isRegister ? (
              <button onClick={handleRegister}>
                Register
              </button>
            ) : (
              <button onClick={handleLogin}>
                Login
              </button>
            )}

            <p style={{ marginTop: "10px" }}>
              {isRegister
                ? "Already have an account?"
                : "Don't have an account?"}



            </p>

          </div>

        </div>
      )}

      <div className={darkMode ? "home dark" : "home"}>

        {/* TOP BAR */}

        <div className="top-bar">

          <button className="mode-btn" onClick={toggleMode}>
            {darkMode ? "Light Mode" : "Dark Mode"}
          </button>

          <button className="logout-btn" onClick={handleLogout}>
            Logout
          </button>

        </div>

        {/* CHAT */}

        <div className="chat-container">

          {messages.map((msg, index) => (
            <div
              key={index}
              className={
                msg.role === "user"
                  ? "user-message"
                  : "ai-message"
              }
            >
              {msg.text}
            </div>
          ))}

          <div ref={chatEndRef}></div>

        </div>

        {messages.length === 0 && (
          <div className="center-text">
            <h1>AI Travel Assistant</h1>
            <p>Ask about hotels, tours, travel plans...</p>
          </div>
        )}

        {/* INPUT */}

        <div className="input-container">

          <input
            type="text"
            placeholder="Ask something..."
            className="prompt-input"
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            onKeyDown={handleKeyPress}
          />

          <button onClick={sendPrompt} className="send-btn">
            Send
          </button>

        </div>

      </div>

    </>
  );
}

export default Home;