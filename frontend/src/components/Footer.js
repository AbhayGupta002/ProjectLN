import React from "react";
import { Globe, Linkedin, Instagram, ShieldCheck } from "lucide-react";
import "../styles/Footer.css";

const Footer = () => {
  return (
    <footer className="nextgem-footer">
      <div className="nextgem-footer-content">
        {/* Brand & Logo Section */}
        <div className="nextgem-brand-col">
          <div className="nextgem-logo-wrapper">
            <div className="app-logo-wrapper footer-logo-wrapper">
              <div className="app-logo-border-sweep"></div>
              <img
                src="/assets/logo-badge.png"
                alt="NextGem Hotel-LuxNes Logo"
                className="app-logo-img"
              />
            </div>
            <div className="nextgem-brand-titles">
              <span className="nextgem-company-name">
                NEXTGEM-TECHNOLOGY
                <span className="ngt-secret-badge" title="NextGem-Technology">
                  NG-T
                </span>
              </span>
              <span className="nextgem-tagline">AI & Cloud Travel Innovation</span>
            </div>
          </div>
          <p className="nextgem-about-desc">
            Next-generation enterprise travel infrastructure powered by intelligent conversational agents,
            safe multi-modal transport orchestration, and bank-grade payment security.
          </p>
        </div>

        {/* Contact Us Section */}
        <div className="nextgem-contact-col">
          <h3 className="nextgem-col-title">CONTACT US: NEXTGEM-TECHNOLOGY</h3>
          <ul className="nextgem-contact-list">
            <li>
              <a
                href="https://nextgem-technology.web.app/"
                target="_blank"
                rel="noopener noreferrer"
                className="nextgem-contact-link"
              >
                <Globe className="nextgem-icon" size={18} />
                <span>nextgem-technology.web.app</span>
              </a>
            </li>
            <li>
              <a
                href="https://www.linkedin.com/company/139843904/admin/dashboard/"
                target="_blank"
                rel="noopener noreferrer"
                className="nextgem-contact-link"
              >
                <Linkedin className="nextgem-icon" size={18} />
                <span>NextGem Technology LinkedIn</span>
              </a>
            </li>
            <li>
              <a
                href="https://www.instagram.com/nextgemtechnology/"
                target="_blank"
                rel="noopener noreferrer"
                className="nextgem-contact-link"
              >
                <Instagram className="nextgem-icon" size={18} />
                <span>@nextgemtechnology</span>
              </a>
            </li>
          </ul>
        </div>

        {/* Legal & Template Protection */}
        <div className="nextgem-legal-col">
          <div className="nextgem-badge">
            <ShieldCheck size={16} /> Proprietary Code & Design Protected
          </div>
          <p className="nextgem-legal-text">
            All rights reserved. No person or organization may copy, clone, distribute, reverse-engineer,
            or utilize this code, codebase templates, design assets, or architecture without prior express written permission.
          </p>
        </div>
      </div>

      <div className="nextgem-footer-bottom">
        <div className="nextgem-bottom-content">
          <p className="nextgem-copyright">
            &copy; {new Date().getFullYear()} <strong>NEXTGEM-TECHNOLOGY (NG-T)</strong>. All Rights Reserved. Proprietary & Confidential.
          </p>
          <div className="nextgem-bottom-links">
            <a href="https://nextgem-technology.web.app/" target="_blank" rel="noopener noreferrer">Official Website</a>
            <span>•</span>
            <a href="https://www.linkedin.com/company/139843904/admin/dashboard/" target="_blank" rel="noopener noreferrer">LinkedIn</a>
            <span>•</span>
            <a href="https://www.instagram.com/nextgemtechnology/" target="_blank" rel="noopener noreferrer">Instagram</a>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
