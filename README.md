# SmritiSetu (??????????) ??

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android](https://img.shields.io/badge/Android-Jetpack%20Compose-green.svg)](https://developer.android.com/jetpack/compose)
[![Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot%203.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![FastAPI](https://img.shields.io/badge/AI%20Microservice-FastAPI%20Python%203.11-teal.svg)](https://fastapi.tiangolo.com)

**AI-Driven Cognitive Gaming & Assistive Memory Platform for Dementia & Alzheimer's Care.**

SmritiSetu ("Bridge of Memory") delivers clinically grounded brain stimulation through gentle, adaptive cognitive exercises tailored for elders and patients in the North-East & Pan-India multilingual regions. An intelligent AI model continuously calibrates task difficulty in real time based on response latencies, error patterns, and hint usage.

---

## ?? Clean Repository Structure

```
SmritiSetu/
+-- SmritiSetu/              # Android Application (Kotlin, Jetpack Compose, Material 3, Navigation 3)
¦   +-- app/src/main/
¦   ¦   +-- java/com/example/smritisetu/
¦   ¦   ¦   +-- data/       # State Management, AuthManager, SharedPreferences, Localized Strings (12 Languages)
¦   ¦   ¦   +-- network/    # Retrofit, OkHttp JWT Interceptor, DTOs & API Clients
¦   ¦   ¦   +-- theme/      # Glassmorphic UI, Themes, Adaptive Typography & Scaling
¦   ¦   ¦   +-- ui/         # Role-based Screens (Patient Games, Leagues, Caregiver Dashboard)
¦   ¦   +-- res/            # Vector drawables, League Badges, Layout resources
¦   +-- gradle/             # Version Catalog (libs.versions.toml) & Gradle 9.1
¦
+-- backend/                 # Backend REST API (Java 17, Spring Boot 3.3.5, Spring Security 6)
¦   +-- src/main/java/com/example/SpringBoot_Bakend/
¦   ¦   +-- config/         # JWT Security, WebConfig, Exception Handlers
¦   ¦   +-- controllers/    # Auth, User, Game, League, Caregiver & Root Health Endpoints
¦   ¦   +-- dto/            # Strongly typed Request/Response Payloads
¦   ¦   +-- entities/       # JPA Entities (User, GameProgress, Reminder, LeagueStatus, DifficultyLog)
¦   ¦   +-- repository/     # Spring Data JPA PostgreSQL Repositories
¦   ¦   +-- service/        # Game Mechanics, Telemetry, Daily Streaks, AI Orchestration
¦   +-- src/main/resources/ # application.properties with environment bindings
¦   +-- .env.example        # Reference environment variables for cloud deployment
¦   +-- pom.xml             # Maven Build Configuration
¦
+-- model/                   # AI Difficulty Engine (Python 3.11, FastAPI, Scikit-learn, XGBoost)
¦   +-- app.py              # FastAPI Microservice & Prediction Endpoints
¦   +-- predictor.py        # ML Model Inference & Adaptive Difficulty Logic
¦   +-- requirements.txt    # Python dependencies
¦
+-- docs/                    # Project Documentation
¦   +-- PRD.md              # Product Requirements Document
¦   +-- SYSTEM_DESIGN.md    # Architecture, Schema Diagrams & Sequence Flows
¦   +-- API_ENDPOINTS.md    # Full REST API Reference & Request/Response Schemas
¦   +-- DEPLOYMENT_GUIDE.md # Cloud Deployment Guide (Render, Supabase, Neon)
¦
+-- render.yaml              # Render Cloud Infrastructure as Code Configuration
```

---

## ?? Key Capabilities

1. **Role-Based Experience**:
   - **Patient Mode**: Accessible, high-contrast gameplay (Match Card, Sequence Memory, Pattern Recall), leagues (Bronze ? Legend), shop perks, and unique 6-digit Patient Link Code (`SM-XXXX`).
   - **Caregiver Dashboard**: Single-screen monitoring showing patient status, XP, league standings, max levels reached, AI cognitive history, and daily care reminders (medicine, hydration, activity).
2. **Multilingual Inclusivity**: Full localizations across 12 Indian & North-Eastern languages (Assamese, Bengali, Bodo, Garo, Hindi, Khasi, Kokborok, Manipuri/Meitei, Mizo, Nepali, Nagamese, English).
3. **Machine Learning Difficulty Calibration**: Evaluates user telemetry every 5 levels to adjust grid size, preview duration, and distraction density safely without frustration.
4. **Offline-First Resilience**: Local persistence with background cloud synchronization upon connectivity.

---

## ?? Documentation Links

- [Product Requirements Document (PRD)](./docs/PRD.md)
- [System Design & Architecture](./docs/SYSTEM_DESIGN.md)
- [REST API Endpoints Reference](./docs/API_ENDPOINTS.md)
- [Cloud Deployment Guide](./docs/DEPLOYMENT_GUIDE.md)

---

## ?? License
This project is licensed under the MIT License - see the LICENSE file for details.
