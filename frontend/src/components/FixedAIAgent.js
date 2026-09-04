import React, { useState, useEffect } from "react";
import AIChatModal from "./AIChatModal";
import "../styles/AIChatModal.css";

function FixedAIAgent() {
  const [isOpen, setIsOpen] = useState(false);

  // Global listener so any action (e.g. Hero strip or quick action) can open the agent
  useEffect(() => {
    const handleOpenChat = () => setIsOpen(true);
    window.addEventListener("open-ai-chat", handleOpenChat);
    return () => window.removeEventListener("open-ai-chat", handleOpenChat);
  }, []);

  return (
    <>
      {/* Fixed Right-Side Agent Button - Only Logo, No extra divs, Does NOT scroll with page */}
      <button
        className="fixed-agent-logo-btn"
        onClick={() => setIsOpen((prev) => !prev)}
        title="AI Travel Agent"
        aria-label="AI Travel Agent"
      >
        <img
          src="/assets/ai-agent-logo.png"
          alt="AI Agent"
          className="fixed-agent-logo-only"
        />
      </button>

      {/* AI Chat Panel */}
      {isOpen && <AIChatModal onClose={() => setIsOpen(false)} />}
    </>
  );
}

export default FixedAIAgent;
