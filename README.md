# SmritiSetu 🧠

**A cognitive care game platform for dementia patients — built to keep the mind gently engaged, one level at a time.**

SmritiSetu ("bridge of memory") is a mobile game app that delivers brain-stimulation exercises to dementia patients through simple, accessible games. An AI model continuously analyzes each patient's performance and adapts difficulty in real time, so the experience stays challenging without ever becoming overwhelming.

---

## ✨ Features

- 🎮 **3 core cognitive games** — Memory Match, Sequence Recall, Daily Reasoning
- 🏆 **League & XP system** — Bronze → Silver → Gold → Platinum progression
- 🤖 **AI Difficulty Engine** — analyzes every 5-level window (time taken, hints used, tries) and adjusts difficulty accordingly
- 💡 **Idle-screen hints** — gentle nudges if the user is inactive
- 🌐 **Multi-language support** — built for India's linguistic diversity (Hindi, Bengali, Tamil, Telugu, and more)
- 📊 **Progress tracking** — per-level and per-window analytics stored for caregivers/clinicians
- 🔒 **Privacy-first** — encrypted patient data, caregiver consent flow

---

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| Mobile App | React Native |
| Backend | Java (Spring Boot, microservices) |
| AI/ML Service | Python (FastAPI + scikit-learn/XGBoost) |
| Relational DB | PostgreSQL (users, leagues, XP) |
| Document DB | MongoDB (level attempt logs) |
| Cache | Redis (session cache, leaderboard) |
| Storage | S3-compatible object storage (game assets) |
| Auth | JWT-based authentication |
| Push Notifications | Firebase Cloud Messaging (FCM) |

---

## 📁 Project Structure

```
smritisetu/
├── mobile-app/              # React Native app
│   ├── src/screens/
│   ├── src/games/
│   ├── src/i18n/
│   └── src/services/api/
├── backend/                  # Java Spring Boot microservices
│   ├── gateway/
│   ├── auth-service/
│   ├── user-service/
│   ├── game-session-service/
│   ├── league-service/
│   └── content-service/
├── ai-engine/                 # Python difficulty analysis service
│   ├── app/
│   ├── models/
│   ├── training/
│   └── logs/
└── docs/
    └── SmritiSetu-System-Design.md
```

---

## 🎮 Games

### 1. Memory Match
Cards are shown for a few seconds, then hidden. The user must recall the card or pattern that was there, choosing from 6 options.

### 2. Sequence Recall
A number or pattern series is displayed (e.g. `1, 5, 9, 89, 12`). The user taps the values back in the same sequence. Difficulty scales with series length and complexity.

### 3. Daily Reasoning
Everyday situational questions (e.g. *"What should we do after we wake up?"*) with multiple-choice answers, reinforcing routine and reasoning skills.

---

## 🤖 How the AI Difficulty Engine Works

1. Every level completion sends `time_taken`, `hints_used`, and `tries_count` to the backend.
2. After every **5 levels**, the backend pulls the current 5-level window and the previous 5-level window.
3. This data is sent to the AI Difficulty Engine, which decides:
   - Whether to **increase**, **decrease**, or **hold** difficulty
   - **By how much** (bounded to a safe delta — no sudden spikes)
4. The new difficulty applies for the **next 5 levels**, and the decision is logged for transparency.
5. XP is awarded per level based on speed, hints, and tries — feeding into the league system.

> Full architecture, ER diagrams, and sequence flows are in [`docs/SmritiSetu-System-Design.md`](./docs/SmritiSetu-System-Design.md).

---

## 🚀 Getting Started

### Prerequisites
- Node.js ≥ 18
- Java 17+ and Maven
- Python 3.10+
- PostgreSQL, MongoDB, Redis (local or Docker)

### Mobile App
```bash
cd mobile-app
npm install
npx react-native run-android   # or run-ios
```

### Backend Services
```bash
cd backend/<service-name>
mvn spring-boot:run
```

### AI Engine
```bash
cd ai-engine
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

### Environment Variables
Create a `.env` file in each service with:
```
DB_URL=
DB_USER=
DB_PASSWORD=
JWT_SECRET=
AI_ENGINE_URL=
```

---

## 🧩 API Overview

| Endpoint | Method | Description |
|---|---|---|
| `/auth/login` | POST | Patient/caregiver login |
| `/user/profile` | GET/PUT | Get or update profile & language |
| `/game/{gameId}/level/{n}/start` | GET | Fetch level config at current difficulty |
| `/game/{gameId}/level/complete` | POST | Submit level performance data |
| `/league/status` | GET | Current league, XP, progress |
| `/content/{lang}/{gameId}` | GET | Localized game content |

---

## 🗺️ Roadmap

- [ ] Core game engine (Memory Match, Sequence Recall, Daily Reasoning)
- [ ] Rule-based difficulty engine (v1)
- [ ] ML-based difficulty engine trained on logged decisions (v2)
- [ ] Multi-language content pipeline
- [ ] Caregiver dashboard (progress insights)
- [ ] Offline mode with sync-on-reconnect

---

## 🤝 Contributing

This project follows phase-wise development — each phase is completed, committed with a professional commit message, and reviewed before moving to the next.

1. Fork the repo
2. Create a feature branch (`git checkout -b feature/game-session-service`)
3. Commit your changes (`git commit -m "feat: add level completion endpoint"`)
4. Push and open a PR

---

## 📄 License

TBD

---

## 👤 Author

**Sathtik Bose**
📧 sathtikbose@gmail.com
