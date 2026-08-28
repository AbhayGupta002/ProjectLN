# 🌍 worldtours.com — AI-Powered Hotel, Tour & Transport Platform

**worldtours.com** is a production-ready, full-stack travel booking application built with Java 17, Spring Boot, React, MySQL, MongoDB, Redis, and Docker.

Powered by **NEXTGEM-TECHNOLOGY**, it features AI itinerary planning, hotel stays with real location & GPS metadata, flight, train, bus, and cab reservations, multi-role security (User, Hotel Partner, Admin), 2FA/lockout authentication, and a persistent 3-mode global theme system (**75% Dark**, **50% Light**, **25% Light**).

---

## 🚀 Tech Stack

- **Backend**: Java 17, Spring Boot 3, Spring Security, Spring Data JPA, JWT, BCrypt, Jackson
- **Frontend**: React 18, React Router v6, Lucide Icons, Scoped CSS Variables
- **Databases**: MySQL 8.0 (Relational / Bookings), MongoDB 6.0 (Chat & Itineraries), Redis 7 (Cache & Sessions)
- **AI / LLM Integration**: OpenAI / Gemini / Ollama with Rule-Based Fallback Orchestration
- **Containerization & Web Server**: Docker, Docker Compose, Nginx Multi-Stage Static Server
- **Payment Gateway**: Razorpay Integration

---

## ☁️ Internet Deployment & Hosting Guide

### Option 1: One-Click Docker Deployment (AWS EC2 / DigitalOcean / VPS)

1. **Clone Repository**:
   ```bash
   git clone https://github.com/AbhayGupta002/ProjectLN.git
   cd ProjectLN
   ```

2. **Configure Environment Secrets**:
   Copy `backend/src/main/resources/application-example.properties` to `backend/src/main/resources/application.properties` and set your production environment variables (Database password, Mail credentials, JWT secret, Razorpay keys).

3. **Build & Start Containers**:
   ```bash
   docker compose up -d --build
   ```

4. **Access Applications**:
   - **Frontend**: `http://<YOUR_SERVER_IP>:3001` or bound to domain `https://worldtours.com`
   - **Backend API**: `http://<YOUR_SERVER_IP>:8080`

---

### Option 2: Serverless Deployment (Vercel / Render / Railway / Netlify)

#### A. Frontend (Vercel / Netlify / Firebase)
- **Root Directory**: `frontend`
- **Build Command**: `npm run build`
- **Output Directory**: `build`
- **Environment Variables**:
  - `REACT_APP_API_URL`: `https://api.yourdomain.com` (Your deployed backend API URL)
- SPA routing rewrite is pre-configured in `frontend/vercel.json`.

#### B. Backend API (Render / Railway / AWS App Runner / Railway)
- **Root Directory**: `backend`
- **Build Command**: `./mvnw clean package -DskipTests` or Dockerfile build.
- **Environment Variables**:
  - `PORT`: `8080`
  - `DB_URL`: `jdbc:mysql://<MYSQL_HOST>:3306/<DB_NAME>`
  - `DB_USERNAME`: `<MYSQL_USER>`
  - `DB_PASSWORD`: `<MYSQL_PASSWORD>`
  - `MONGO_URI`: `mongodb://<MONGO_HOST>:27017/ProjectLN`
  - `JWT_SECRET`: `<YOUR_SECURE_JWT_KEY>`

---

## 🔐 Default Credentials & Verification

- **Default User Account**:
  - **Email**: `hotelluxnes@gmail.com`
  - **Password**: `password123`
  - **Role**: `USER`

- **Default Admin Account**:
  - **Email**: `admin@nextgem.com`
  - **Password**: `AdminSecretPass123!`
  - **Role**: `ADMIN`

- **Automated Verification Suites**:
  - Backend API Suite: `./test_all_apis.sh http://localhost:8080`
  - Security RBAC Suite: `bash verify_roles.sh`

---

## 🛡️ License & Copyright

Designed & Developed by **NEXTGEM-TECHNOLOGY**  
Official Website: [https://nextgem-technology.web.app/](https://nextgem-technology.web.app/)  
LinkedIn: [NextGem Technology](https://www.linkedin.com/company/139843904/)  
Instagram: [@nextgemtechnology](https://www.instagram.com/nextgemtechnology/)
