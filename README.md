# SmritiSetu (स्मृति सेतु) 🧠🌉
> **AI-Powered Cognitive Health & Dementia Care Companion**

[![Android](https://img.shields.io/badge/Android-Jetpack%20Compose-3DDC84?logo=android&logoColor=white)](SmritiSetu/)
[![Backend](https://img.shields.io/badge/Backend-Spring%20Boot%203-6DB33F?logo=springboot&logoColor=white)](backend/)
[![AI Model](https://img.shields.io/badge/AI%20Service-FastAPI%20%2F%20Python-009688?logo=fastapi&logoColor=white)](model/)
[![Database](https://img.shields.io/badge/Database-PostgreSQL%20%2F%20Supabase-336791?logo=postgresql&logoColor=white)](https://supabase.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📖 Overview

**SmritiSetu** ("Bridge to Memory") is a holistic digital therapeutic platform engineered to support individuals living with Mild Cognitive Impairment (MCI), early-stage dementia, and age-related memory decline. By combining **clinically grounded cognitive training**, **adaptive Machine Learning difficulty calibration**, and **real-time caregiver monitoring**, SmritiSetu delivers a safe, empowering, and engaging environment for patients and peace of mind for families.

---

## 🏛️ Project Architecture

The repository is structured into modular, production-ready components:

```
SmritiSetu/
├── SmritiSetu/         # 📱 Native Android App (Kotlin, Jetpack Compose, Room DB, WorkManager)
├── backend/            # ⚙️ Enterprise REST API (Spring Boot 3, Java 17, JPA, PostgreSQL)
├── model/              # 🤖 Cognitive AI Microservice (Python, FastAPI, Scikit-Learn)
├── docs/               # 📚 Comprehensive Project Documentation & Architecture
│   ├── PRD.md
│   ├── SYSTEM_DESIGN.md
│   ├── API_ENDPOINTS.md
│   └── DEPLOYMENT_GUIDE.md
└── README.md           # 📄 Root Project Guide
```

---

## ✨ Key Features

### 1. 👴 Elderly-Friendly & Accessible Interface
- **High-Contrast Design & Scalable Typography**: High-visibility color schemes, large touch targets (min 48dp), and adjustable text sizes.
- **Cognitive Ease**: Minimal distraction, large visual icons, multilingual support, and voice prompts.

### 2. 🎭 Role-Based Architecture
- **Patient Experience**: Full access to cognitive training games, league leaderboards, personal streaks, rewards, and care reminders. Generates a unique 6-character linking code (`SM-XXXX`).
- **Caregiver Live Dashboard**: Dedicated single-screen monitoring console displaying patient division, XP, coin balance, active streaks, highest game levels reached, telemetry history, and remote care reminder management.

### 3. 🧠 Clinically Grounded Cognitive Games
- **Match The Card (Visual Memory)**: Pair-matching exercises assessing visual recall, pattern recognition, and focus.
- **Pattern Recall (Working Memory)**: Sequence memorization challenges training spatial awareness and executive function.

### 4. 🤖 AI Dynamic Difficulty Calibration
- Collects real-time telemetry (completion speed, error rates, hint utilization).
- Evaluates cognitive fatigue and performance trends via an AI microservice to dynamically adjust game difficulty (grid size, preview duration, distraction elements).

### 5. 📴 Offline-First Sync Engine
- Local SQLite caching via **Room Database** allows seamless offline play.
- Background sync queue powered by **Android WorkManager** automatically uploads progress and pulls updates when connectivity is restored.

### 6. 🏆 Positive Gamification & Habit Building
- Monthly League tiers (Bronze, Silver, Gold, Platinum, Diamond) reset dynamically.
- Daily streaks, XP incentives, milestone badges, and cosmetic avatars.

---

## 🚀 Environment & Setup Guide

### 1. Spring Boot Backend (`backend/`)

Create an environment file at `backend/.env` (or configure environment variables in your deployment platform such as Render/Railway):

```properties
# Database Configuration (PostgreSQL / Supabase)
DB_URL=jdbc:postgresql://<SUPABASE_HOST>:5432/<DB_NAME>?sslmode=require
DB_USERNAME=postgres.<PROJECT_REF>
DB_PASSWORD=<YOUR_DATABASE_PASSWORD>

# JWT Security
JWT_SECRET=c31677353f478474288b839ef42616f728be7a0aa4c66e2c3a5043bf725b8214
JWT_EXPIRATION_MS=86400000

# AI Microservice Integration URL
AI_SERVICE_URL=https://smritisetuai.onrender.com
PORT=8080
```

**Run Locally:**
```bash
cd backend
./mvnw clean spring-boot:run
```

---

### 2. AI Model Service (`model/`)

Create an environment file at `model/.env`:

```properties
PORT=8000
PYTHON_VERSION=3.11.8
```

**Run Locally:**
```bash
cd model
pip install -r requirements.txt
uvicorn app:app --host 0.0.0.0 --port 8000 --reload
```

---

### 3. Android Application (`SmritiSetu/`)

1. Open `SmritiSetu/` in **Android Studio** (Ladybug / Koala or newer).
2. Ensure JDK 17+ is selected under **Project Structure > SDK Location > Gradle JDK**.
3. Verify or update the backend base URL in `app/src/main/java/com/example/smritisetu/data/api/RetrofitClient.kt`:
   - Local Emulator: `http://10.0.2.2:8080/`
   - Production / Cloud: `https://your-backend.onrender.com/`
4. Build and run on an emulator or physical Android device.

---

## 📚 Documentation Index

| Document | Description |
| :--- | :--- |
| [**`docs/PRD.md`**](docs/PRD.md) | Full Product Requirements, target demographics, functional specifications, and user journeys. |
| [**`docs/SYSTEM_DESIGN.md`**](docs/SYSTEM_DESIGN.md) | Architecture diagrams, database schemas, AI telemetry pipeline, security, and offline sync. |
| [**`docs/API_ENDPOINTS.md`**](docs/API_ENDPOINTS.md) | Comprehensive REST API documentation, request/response models, and headers. |
| [**`docs/DEPLOYMENT_GUIDE.md`**](docs/DEPLOYMENT_GUIDE.md) | Step-by-step instructions for deploying to Supabase PostgreSQL, Render Web Services, and Android APK generation. |

---

## 🛡️ License

This project is licensed under the [MIT License](LICENSE).
