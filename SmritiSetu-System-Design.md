# SmritiSetu — System Design Document

**Mobile Cognitive Care Game for Dementia Patients**
Stack: React Native · Java (Spring Boot) · Python AI Service · PostgreSQL/MongoDB

---

## 1. High-Level Architecture

```mermaid
graph TB
    subgraph Client["📱 React Native App"]
        UI[Game UI Layer]
        LangEngine[i18n / Multi-language Engine]
        LocalCache[Local Cache - Session Buffer]
        UI --> LangEngine
        UI --> LocalCache
    end

    subgraph Gateway["API Gateway"]
        GW[Spring Cloud Gateway / Nginx]
        Auth[Auth Service - JWT]
    end

    subgraph Backend["☕ Java Backend (Spring Boot Microservices)"]
        UserSvc[User & Profile Service]
        GameSvc[Game Session Service]
        LeagueSvc[League & XP Service]
        StatsSvc[Level Stats Aggregator]
        NotifSvc[Notification Service]
    end

    subgraph AI["🧠 AI Difficulty Engine (Python)"]
        Ingest[Data Ingestion API]
        Model[ML Model - Difficulty Classifier]
        Trainer[Feedback Loop / Retraining Job]
    end

    subgraph Data["Data Layer"]
        RDBMS[(PostgreSQL - Users, Leagues, XP)]
        NoSQL[(MongoDB - Level Attempt Logs)]
        Redis[(Redis - Session Cache / Leaderboard)]
        S3[(Object Storage - Card Assets, Audio)]
    end

    Client -->|HTTPS/REST| GW
    GW --> Auth
    GW --> UserSvc
    GW --> GameSvc
    GW --> LeagueSvc
    GameSvc -->|every 5 levels| Ingest
    Ingest --> Model
    Model -->|difficulty delta| GameSvc
    Model --> Trainer
    Trainer -->|periodic retrain| Model

    UserSvc --> RDBMS
    LeagueSvc --> RDBMS
    GameSvc --> NoSQL
    GameSvc --> Redis
    StatsSvc --> NoSQL
    UserSvc --> S3
```

---

## 2. Core Modules

| Module | Responsibility | Tech |
|---|---|---|
| Auth Service | Login, JWT issue/refresh, patient/caregiver roles | Spring Security + JWT |
| User & Profile Service | Patient profile, language pref, medical notes (optional) | Spring Boot + PostgreSQL |
| Game Session Service | Starts/ends level, records time/hints/tries, requests difficulty | Spring Boot |
| League & XP Service | XP calculation, league promotion/demotion | Spring Boot + PostgreSQL |
| Stats Aggregator | Rolls up last-5 vs previous-5 level windows | Spring Boot + MongoDB aggregation |
| AI Difficulty Engine | Classifies performance trend → difficulty delta | Python (FastAPI) + scikit-learn/XGBoost |
| Notification Service | Reminders, screen-idle hints trigger | FCM |
| i18n Engine | Multi-language content delivery (10+ Indian languages) | react-native-localize + backend content service |

---

## 3. Database Schema (ER Diagram)

```mermaid
erDiagram
    USER ||--o{ GAME_PROGRESS : has
    USER ||--|| LEAGUE_STATUS : has
    USER {
        uuid id PK
        string name
        string preferred_language
        string caregiver_contact
        timestamp created_at
    }

    GAME ||--o{ LEVEL : contains
    GAME {
        uuid id PK
        string name
        string type
    }

    LEVEL {
        uuid id PK
        uuid game_id FK
        int level_number
        int difficulty_score
    }

    GAME_PROGRESS ||--o{ LEVEL_ATTEMPT : logs
    GAME_PROGRESS {
        uuid id PK
        uuid user_id FK
        uuid game_id FK
        int current_level
        int current_difficulty
        timestamp last_played
    }

    LEVEL_ATTEMPT {
        uuid id PK
        uuid progress_id FK
        uuid level_id FK
        int time_taken_sec
        int hints_used
        int tries_count
        int xp_earned
        timestamp played_at
    }

    LEAGUE_STATUS {
        uuid id PK
        uuid user_id FK
        string current_league
        int total_xp
        timestamp promoted_at
    }

    DIFFICULTY_LOG {
        uuid id PK
        uuid user_id FK
        uuid game_id FK
        int window_start_level
        int window_end_level
        json input_features
        string ai_decision
        float difficulty_delta
        timestamp created_at
    }
```

---

## 4. Level Attempt → AI Difficulty Flow (Sequence Diagram)

```mermaid
sequenceDiagram
    participant App as React Native App
    participant GW as API Gateway
    participant GameSvc as Game Session Service
    participant Mongo as MongoDB (Attempt Logs)
    participant AI as AI Difficulty Engine
    participant PG as PostgreSQL

    App->>GW: POST /level/complete (time, hints, tries)
    GW->>GameSvc: forward request
    GameSvc->>Mongo: store LEVEL_ATTEMPT
    GameSvc->>GameSvc: increment level counter

    alt every 5th level completed
        GameSvc->>Mongo: fetch last 5 + previous 5 attempts
        GameSvc->>AI: POST /analyze {prev5, last5, user_id, game_id}
        AI->>AI: run difficulty classifier model
        AI-->>GameSvc: {increase: true/false, delta: 0.0-1.0, next_difficulty}
        GameSvc->>PG: update GAME_PROGRESS.current_difficulty
        GameSvc->>Mongo: log DIFFICULTY_LOG entry
    end

    GameSvc->>PG: update XP + League
    GameSvc-->>App: {xp_earned, new_league?, next_level_config}
```

---

## 5. AI Difficulty Engine — Internal Design

```mermaid
graph LR
    subgraph Input["Input Features (per 5-level window)"]
        F1[Avg time taken]
        F2[Avg hints used]
        F3[Avg tries]
        F4[Trend vs previous window]
        F5[Game type]
    end

    F1 & F2 & F3 & F4 & F5 --> Norm[Feature Normalization]
    Norm --> Model{Difficulty Classifier}
    Model -->|Improving fast| Up[Increase difficulty +X%]
    Model -->|Stable| Hold[Keep same difficulty]
    Model -->|Struggling| Down[Decrease difficulty -X%]
    Up & Hold & Down --> Clamp[Clamp within safe bounds<br/>for dementia-friendly pacing]
    Clamp --> Output[Return next_difficulty + reasoning]
```

**Model approach:** start with a rule-weighted scoring model (explainable, safe for medical-adjacent use), log every decision + outcome into `DIFFICULTY_LOG`, then later train a supervised model (XGBoost/LightGBM) on accumulated logs to replace/augment the rules. Keep decisions bounded (never spike difficulty for cognitively vulnerable users) and always log the "why" for caregiver/clinician transparency.

---

## 6. Game Module Breakdown

```mermaid
graph TD
    subgraph G1["Game 1: Memory Match"]
        G1a[Show N cards for T seconds]
        G1b[Hide cards]
        G1c[Ask: which card/pattern was where?]
        G1d[6-option multiple choice]
        G1a-->G1b-->G1c-->G1d
    end

    subgraph G2["Game 2: Sequence Recall"]
        G2a[Display number/pattern series]
        G2b[User taps numbers in same sequence]
        G2c[Difficulty = series length + complexity]
        G2a-->G2b-->G2c
    end

    subgraph G3["Game 3: Daily Reasoning"]
        G3a[Show situational question]
        G3b[Present 3-4 options]
        G3c[User selects best answer]
        G3a-->G3b-->G3c
    end
```

---

## 7. Multi-Language Support Design

```mermaid
graph LR
    Device[Device Locale Detected] --> LangSelect[Language Selector Screen]
    LangSelect --> Store[Store pref in USER.preferred_language]
    Store --> ContentAPI[Content Delivery Service]
    ContentAPI --> JSON1[hi.json]
    ContentAPI --> JSON2[bn.json]
    ContentAPI --> JSON3[ta.json]
    ContentAPI --> JSON4[te.json]
    ContentAPI --> JSONn[...more Indian languages]
    JSON1 & JSON2 & JSON3 & JSON4 & JSONn --> App[Rendered in-app]
```

Content (question text, hint text, option labels) stored as versioned JSON bundles per language, served via a lightweight content endpoint so new languages/games don't need app releases.

---

## 8. XP & League System

```mermaid
stateDiagram-v2
    [*] --> Bronze
    Bronze --> Silver: XP threshold reached
    Silver --> Gold: XP threshold reached
    Gold --> Platinum: XP threshold reached
    Silver --> Bronze: inactivity/regression rule (optional)
    Gold --> Silver: inactivity/regression rule (optional)
    Platinum --> [*]: max league
```

XP formula (example): `xp = base_xp - (hints_used * hint_penalty) - (tries_count * try_penalty) + speed_bonus`

---

## 9. API Endpoints (Summary)

| Endpoint | Method | Service | Purpose |
|---|---|---|---|
| `/auth/login` | POST | Auth | Login patient/caregiver |
| `/user/profile` | GET/PUT | User Service | Get/update profile & language |
| `/game/{gameId}/level/{n}/start` | GET | Game Session | Fetch level config at current difficulty |
| `/game/{gameId}/level/complete` | POST | Game Session | Submit time, hints, tries |
| `/ai/analyze` | POST | AI Engine | Internal — analyze 5+5 window |
| `/league/status` | GET | League Service | Current league, XP, progress |
| `/content/{lang}/{gameId}` | GET | Content Service | Localized game content |

---

## 10. Non-Functional Requirements

- **Accessibility-first UI:** large tap targets, high contrast, minimal text, audio cues (dementia-friendly UX).
- **Offline resilience:** local buffering of level attempts if network drops, sync on reconnect.
- **Bounded AI decisions:** difficulty never jumps more than a safe delta per adjustment window.
- **Data privacy:** patient data encrypted at rest (AES-256) and in transit (TLS); caregiver consent flow for any data collection.
- **Low cognitive load:** no unnecessary animations/timers that induce stress.
- **Scalability:** stateless Java services behind gateway, horizontally scalable; MongoDB for high-write attempt logs, PostgreSQL for relational league/user data.

---

## 11. Suggested Repo/Module Structure

```
smritisetu/
├── mobile-app/              # React Native
│   ├── src/screens/
│   ├── src/games/
│   ├── src/i18n/
│   └── src/services/api/
├── backend/
│   ├── gateway/
│   ├── auth-service/
│   ├── user-service/
│   ├── game-session-service/
│   ├── league-service/
│   └── content-service/
└── ai-engine/
    ├── app/ (FastAPI)
    ├── models/
    ├── training/
    └── logs/
```
