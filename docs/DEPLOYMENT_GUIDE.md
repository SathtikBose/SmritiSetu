# SmritiSetu Deployment Guide ??

This document provides step-by-step instructions for deploying the **SmritiSetu** ecosystem on [Render](https://render.com) and connecting to [Supabase](https://supabase.com).

---

## 1. Architecture Overview

- **Android Client (`SmritiSetu/`)**: Native Jetpack Compose Android app with offline-first caching and Retrofit client.
- **Backend Service (`backend/`)**: Java 17 / Spring Boot 3.3.5 REST API with JWT security and PostgreSQL persistence.
- **AI Engine (`model/`)**: Python 3.11 / FastAPI microservice running calibrated difficulty models.
- **Database**: Managed PostgreSQL on Supabase or Render PostgreSQL.

---

## 2. Environment Variables

### Backend (`backend/`)
| Variable | Required | Example Value | Description |
|---|:---:|---|---|
| `DB_URL` | **Yes** | `jdbc:postgresql://db.xxx.supabase.co:5432/postgres?sslmode=require` | PostgreSQL JDBC connection URL |
| `DB_USERNAME` | **Yes** | `postgres` | Database username |
| `DB_PASSWORD` | **Yes** | `your_secure_password` | Database password |
| `JWT_SECRET` | **Yes** | `c31677353f478474288b839ef42616f728be7a0aa4c66e2c3a5043bf725b8214` | 256-bit JWT secret key |
| `JWT_EXPIRATION_MS` | No | `86400000` | JWT expiration (24 hours) |
| `AI_SERVICE_URL` | No | `https://smritisetuai.onrender.com` | Python FastAPI AI URL |
| `GOOGLE_CLIENT_ID` | No | `xxx.apps.googleusercontent.com` | Google OAuth2 Client ID |
| `PORT` | Auto | `8080` | Assigned automatically by Render |

### AI Model (`model/`)
| Variable | Required | Example Value | Description |
|---|:---:|---|---|
| `PYTHON_VERSION` | No | `3.11.8` | Python runtime version |
| `PORT` | Auto | `8000` | Assigned automatically by Render |

---

## 3. Deploying on Render

### A. Deploy Backend Web Service
1. **Repository**: `SathtikBose/SmritiSetu`
2. **Branch**: `Harpreet` or `main`
3. **Root Directory**: `backend`
4. **Runtime**: `Java`
5. **Build Command**: `chmod +x ./mvnw && ./mvnw clean package -DskipTests`
6. **Start Command**: `java -jar target/SpringBoot-Bakend-0.0.1-SNAPSHOT.jar`
7. Add the environment variables from the table above.

### B. Deploy AI Model Service
1. **Root Directory**: `model`
2. **Runtime**: `Python`
3. **Build Command**: `pip install -r requirements.txt && python -c "from predictor import DifficultyPredictor; DifficultyPredictor()"`
4. **Start Command**: `uvicorn app:app --host 0.0.0.0 --port $PORT`
5. **Health Check Path**: `/health`

---

## 4. Verification Endpoints

- **Backend Health**: `GET https://<backend-url>/` or `GET https://<backend-url>/auth/health`
- **AI Model Health**: `GET https://<ai-url>/health`
