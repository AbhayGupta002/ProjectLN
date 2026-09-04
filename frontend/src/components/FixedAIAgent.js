import React, { useState, useEffect } from "react";
import AIChatModal from "./AIChatModal";
import { MessageSquare, X } from "lucide-react";
import "../styles/AIChatModal.css";

const LOGO_SRC = "/assets/ai-agent-logo.png";

function FixedAIAgent() {
  const [isOpen, setIsOpen] = useState(false);
  const [hasNewPrompt, setHasNewPrompt] = useState(true);

  // Global event listener to allow other components (Home hero, Dashboard sidebar, etc.) to open the agent
  useEffect(() => {
    const handleOpenChat = () => setIsOpen(true);
    window.addEventListener("open-ai-chat", handleOpenChat);
    return () => window.removeEventListener("open-ai-chat", handleOpenChat);
  }, []);

  // Dismiss promotional tooltip after 12 seconds or when opened
  useEffect(() => {
    if (isOpen) setHasNewPrompt(false);
    const timer = setTimeout(() => setHasNewPrompt(false), 12000);
    return () => clearTimeout(timer);
  }, [isOpen]);

  const toggleChat = () => {
    setIsOpen((prev) => !prev);
    setHasNewPrompt(false);
  };

  return (
    <>
      {/* Fixed Right-Side Floating Panel Trigger (Does NOT scroll with page) */}
      <aside className="fixed-agent-panel-wrapper" aria-label="AI Travel Agent">
        {hasNewPrompt && !isOpen && (
          <div className="agent-floating-tip" onClick={toggleChat}>
            <span className="tip-close" onClick={(e) => { e.stopPropagation(); setHasNewPrompt(false); }}>×</span>
            <strong>Ask AI Agent ✨</strong>
            <p>Plan flights, trains, stays & instant bookings!</p>
          </div>
        )}

        <button
          className={`fixed-agent-trigger-btn ${isOpen ? "active" : ""}`}
          onClick={toggleChat}
          title={isOpen ? "Close AI Agent" : "Chat with AI Agent"}
          aria-expanded={isOpen}
        >
          <div className="agent-logo-frame">
            <img src={LOGO_SRC} alt="AI Agent Logo" className="agent-trigger-logo" />
            <span className="agent-online-pulse" title="Agent Online"></span>
          </div>
          <div className="agent-trigger-text">
            <span className="agent-trigger-title">AI Agent</span>
            <span className="agent-trigger-status">Online 24/7</span>
          </div>
          <div className="agent-toggle-icon">
            {isOpen ? <X size={18} /> : <MessageSquare size={18} />}
          </div>
        </button>
      </aside>

      {/* AI Chat Modal / Slide Panel */}
      {isOpen && <AIChatModal onClose={() => setIsOpen(false)} />}
    </>
  );
}

export default FixedAIAgent;
