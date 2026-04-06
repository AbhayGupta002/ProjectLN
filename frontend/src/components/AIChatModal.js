import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import "../styles/AIChatModal.css";

function AIChatModal({ onClose, animating }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [darkMode, setDarkMode] = useState(true);

  const chatEndRef = useRef(null);

  // 🌗 Load theme
  useEffect(() => {
    const savedTheme = localStorage.getItem("ai-theme");
    if (savedTheme) setDarkMode(savedTheme === "dark");
  }, []);

  // 💾 Save theme
  useEffect(() => {
    localStorage.setItem("ai-theme", darkMode ? "dark" : "light");
  }, [darkMode]);

  // 📜 Auto scroll
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading]);

  const sendMessage = async (customInput) => {
    const text = customInput || input;
    if (!text.trim()) return;

    const userMessage = { role: "user", text };
    setMessages((prev) => [...prev, userMessage]);

    setLoading(true);
    setInput("");

    try {
      const res = await axios.post("http://localhost:8080/api/ai/prompt", {
        prompt: text,
      });

      // 🔥 Handle list (hotel/tour) or text
      if (Array.isArray(res.data)) {
        setMessages((prev) => [
          ...prev,
          { role: "ai", type: "list", data: res.data },
        ]);
      } else {
        setMessages((prev) => [
          ...prev,
          { role: "ai", text: res.data },
        ]);
      }
    } catch {
      setMessages((prev) => [
        ...prev,
        { role: "ai", text: "⚠️ Error connecting to AI" },
      ]);
    }

    setLoading(false);
  };

  // ⌨️ Enter support
  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const clearChat = () => setMessages([]);

  return (
    <div className={`ai-modal-overlay ${darkMode ? "dark" : "light"}`}>
      <div className={`ai-modal ${animating ? "open" : "close"}`}>

        {/* HEADER */}
        <div className="ai-header">
          <h3>🤖 AI Assistant</h3>

          <div className="ai-actions">
            <button onClick={() => setDarkMode(!darkMode)}>
              {darkMode ? "🌙" : "☀️"}
            </button>
            <button onClick={clearChat}>🗑</button>
            <button onClick={onClose}>✖</button>
          </div>
        </div>

        {/* 🔥 QUICK ACTIONS */}
        <div className="quick-actions">
          <button onClick={() => sendMessage("Find hotels in Indore under 5000")}>
            🏨 Hotels
          </button>
          <button onClick={() => sendMessage("Show tours in Manali under 1000")}>
            ✈️ Tours
          </button>
          <button onClick={() => sendMessage("Cheap travel under 2000")}>
            💰 Budget
          </button>
          <button onClick={() => sendMessage("Popular destinations in India")}>
            📍 Popular
          </button>
        </div>

        {/* BODY */}
        <div className="ai-body">
          {messages.length === 0 && (
            <div className="ai-welcome">
              <h3>👋 Welcome!</h3>
              <p>Try asking:</p>
              <ul>
                <li>Hotel in Indore under 5000</li>
                <li>Tour in Manali under 1000</li>
              </ul>
            </div>
          )}

          {messages.map((msg, i) => (
            <div key={i}>
              {msg.role === "user" && (
                <div className="user-msg">{msg.text}</div>
              )}

              {msg.role === "ai" && msg.type === "list" ? (
                <div className="result-cards">
                  {msg.data.map((item, index) => (
                    <div key={index} className="card">
                      <h4>{item.name}</h4>
                      <p>📍 {item.location}</p>
                      <p>💰 ₹{item.price}</p>
                      <button className="book-btn">Book Now</button>
                    </div>
                  ))}
                </div>
              ) : (
                msg.role === "ai" && (
                  <div className="ai-msg">{msg.text}</div>
                )
              )}
            </div>
          ))}

          {loading && <div className="ai-msg typing">Typing</div>}

          <div ref={chatEndRef} />
        </div>

        {/* FOOTER */}
        <div className="ai-footer">
          <textarea
            placeholder="Ask: Hotel in Indore under 5000"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyPress}
            rows={1}
          />
          <button onClick={() => sendMessage()}>Send</button>
        </div>
      </div>
    </div>
  );
}

export default AIChatModal;