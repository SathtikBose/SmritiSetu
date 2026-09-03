# 🛠️ SmritiSetu: Backend Missing Requirements & Action Plan

This document outlines everything required in the **Spring Boot Backend (`SpringBoot-Bakend`)** to achieve 100% feature parity with the **Android Frontend (`SmritiSetu`)** and the **Python AI Model (`model/`)**.

---

## 📌 1. Database Schema & Entity Updates

The `User` entity and PostgreSQL tables currently lack fields required by the latest mobile screens, economy, and caregiver flows.

### A. Update `users` Table / `User.java` Entity
Add the following fields:

```java
@Entity
@Table(name = "users")
public class User {
    // ... existing fields (id, username, password, name, role, preferredLanguage, authProvider) ...

    @Column(nullable = false, unique = true, length = 10)
    private String patientLinkCode; // Unique 6-character code (e.g. "SM-8492") auto-generated on signup

    @Column(nullable = true, length = 10)
    private String linkedPatientCode; // For Caregivers: stores the linked patient's link code

    @Column(nullable = false)
    private Integer coins = 1000; // Starting balance: 1000 coins

    @Column(nullable = false)
    private Integer hintsCount = 0; // Extra hint perks in inventory

    @Column(nullable = false)
    private Integer skipLevelCount = 0; // Skip level perks in inventory

    @Column(nullable = true)
    private String phone; // e.g. "+91 98765 43210"

    @Column(nullable = true)
    private String gender; // "Male", "Female", "Other"

    @Column(nullable = true)
    private Integer age; // e.g. 68

    @Column(nullable = true)
    private String avatarUri; // Photo URI / path

    @Column(nullable = false)
    private Integer highestUnlockedLevel = 5; // First 5 levels unlocked by default
}
```

---

## 📌 2. Missing Endpoints & API Contracts

### A. Caregiver & Patient Linking by Code

#### 1. Link Patient by 6-Digit Code
- **Method**: `POST`
- **Path**: `/caregiver/link-by-code`
- **Auth**: `Bearer <JWT_TOKEN>` (Caregiver Role)
- **Request Body**:
  ```json
  {
    "linkCode": "SM-8492"
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "success": true,
    "message": "Patient linked successfully",
    "patient": {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "name": "Dr. Ananya Sharma",
      "linkCode": "SM-8492",
      "age": 68,
      "gender": "Female"
    }
  }
  ```

#### 2. Get Linked Patient Summary (For Caregiver Dashboard)
- **Method**: `GET`
- **Path**: `/caregiver/patient/summary`
- **Auth**: `Bearer <JWT_TOKEN>` (Caregiver Role)
- **Response `200 OK`**:
  ```json
  {
    "patientId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "name": "Dr. Ananya Sharma",
    "linkCode": "SM-8492",
    "age": 68,
    "gender": "Female",
    "leagueTier": "Silver Division",
    "totalXp": 1450,
    "coins": 1000,
    "highestLevelReached": 5,
    "totalGamesPlayed": 12,
    "recentLogs": [
      {
        "gameName": "MatchTheCard",
        "level": 5,
        "timeTakenSec": 58,
        "triesCount": 7,
        "idleHintsCount": 2,
        "perkHintsCount": 1,
        "difficulty": "EASY",
        "completedAt": "2026-09-03T10:30:00Z"
      }
    ]
  }
  ```

---

### B. Cognitive Perks Shop & Inventory System

#### 1. Get Available Shop Items
- **Method**: `GET`
- **Path**: `/shop/items`
- **Response `200 OK`**:
  ```json
  [
    {
      "id": "PERK_HINT",
      "name": "Extra Hint",
      "costCoins": 1000,
      "description": "Auto-highlights unmatched card pair"
    },
    {
      "id": "PERK_SKIP",
      "name": "Skip Level",
      "costCoins": 2000,
      "description": "Instantly completes level and awards full XP & coins"
    }
  ]
  ```

#### 2. Buy Perk with Coins
- **Method**: `POST`
- **Path**: `/shop/buy`
- **Auth**: `Bearer <JWT_TOKEN>`
- **Request Body**:
  ```json
  {
    "perkType": "HINT" // "HINT" or "SKIP_LEVEL"
  }
  ```
- **Business Logic**:
  - Verify user has sufficient coins (1,000 for HINT, 2,000 for SKIP_LEVEL).
  - Deduct coins and increment `hintsCount` or `skipLevelCount`.
- **Response `200 OK`**:
  ```json
  {
    "success": true,
    "remainingCoins": 0,
    "hintsCount": 1,
    "skipLevelCount": 0
  }
  ```

#### 3. Use / Consume Perk in Level
- **Method**: `POST`
- **Path**: `/user/perks/use`
- **Auth**: `Bearer <JWT_TOKEN>`
- **Request Body**:
  ```json
  {
    "perkType": "HINT" // or "SKIP_LEVEL"
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "success": true,
    "remainingCount": 0
  }
  ```

---

### C. Password Management & Security

#### 1. Change Password (Logged In)
- **Method**: `POST`
- **Path**: `/auth/change-password`
- **Auth**: `Bearer <JWT_TOKEN>`
- **Request Body**:
  ```json
  {
    "currentPassword": "oldPassword123",
    "newPassword": "newSecurePassword456"
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "success": true,
    "message": "Password updated successfully"
  }
  ```

#### 2. Forgot Password OTP Flow
- **`POST /auth/forgot-password`**: `{ "email": "user@example.com" }` -> Generates & sends 6-digit OTP.
- **`POST /auth/verify-otp`**: `{ "email": "user@example.com", "otp": "123456", "newPassword": "newPassword" }` -> Validates OTP and updates password.

---

### D. Daily Care Reminders (Delete Support)

- **Method**: `DELETE`
- **Path**: `/caregiver/patient/{patientId}/reminders/{reminderId}`
- **Auth**: `Bearer <JWT_TOKEN>` (Caregiver Role)
- **Response `204 No Content`**

---

## 📌 3. Telemetry & AI Model Integration Bridge

Currently, `GameService.java` uses a hardcoded rule (`if (time < 20) diff++ else diff--`). This must be replaced with a call to the **Python FastAPI AI Microservice**.

### A. Update `LevelAttemptRequest.java` DTO
Update the incoming telemetry DTO to capture rich metrics:

```java
@Data
public class LevelAttemptRequest {
    @NotNull private UUID gameId;
    @NotNull private Integer level;
    @NotNull @Min(0) private Integer timeTakenSec;
    @NotNull @Min(0) private Long timeTakenMs;
    @NotNull @Min(1) private Integer triesCount;
    @NotNull @Min(2) private Integer totalCards;
    @NotNull @Min(0) private Integer idleHintsCount; // Inactivity auto-highlights
    @NotNull @Min(0) private Integer perkHintsCount; // Purchased perks used
    private Boolean syncedOffline = false;
}
```

### B. Update `LevelAttemptResponse.java` DTO
Include coins earned and next difficulty:

```java
@Data
@Builder
public class LevelAttemptResponse {
    private Boolean passed;
    private Integer xpEarned; // +15 XP
    private Integer coinsEarned; // +200 Coins
    private Integer totalCoins;
    private Integer totalXp;
    private Integer nextUnlockedLevel;
    private String predictedDifficulty; // "EASY", "NORMAL", "HARD"
    private String difficultyReasoning;
}
```

### C. Connect to Python AI Model (`POST http://localhost:8000/predict_difficulty`)
When a user completes **every 5th level** (e.g. Level 5, 10, 15, 20...):
1. Query the last 5 level attempts for this user from `level_attempts` table.
2. Send HTTP POST request via `RestTemplate` or `WebClient` to `http://localhost:8000/predict_difficulty`:
   ```json
   {
     "user_id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
     "user_age": 68,
     "game_name": "MatchTheCard",
     "last_5_levels": [
       { "level": 1, "time_taken_ms": 28000, "tries_count": 3, "total_cards": 4, "idle_hints_triggered": 0, "perk_hints_used": 0, "difficulty": "EASY" },
       { "level": 2, "time_taken_ms": 34000, "tries_count": 4, "total_cards": 6, "idle_hints_triggered": 0, "perk_hints_used": 0, "difficulty": "EASY" },
       { "level": 3, "time_taken_ms": 42000, "tries_count": 5, "total_cards": 8, "idle_hints_triggered": 1, "perk_hints_used": 0, "difficulty": "EASY" },
       { "level": 4, "time_taken_ms": 49000, "tries_count": 6, "total_cards": 10, "idle_hints_triggered": 1, "perk_hints_used": 0, "difficulty": "EASY" },
       { "level": 5, "time_taken_ms": 58000, "tries_count": 7, "total_cards": 12, "idle_hints_triggered": 2, "perk_hints_used": 1, "difficulty": "EASY" }
     ]
   }
   ```
3. Receive the response:
   ```json
   {
     "predicted_difficulty": "NORMAL",
     "confidence": 0.94,
     "suggested_timer_seconds": 100,
     "suggested_idle_hint_seconds": 8,
     "cognitive_performance_index": 0.76,
     "fatigue_detected": false,
     "clinical_rationale": "Player maintained high accuracy across levels with low idle hints."
   }
   ```
4. Save the prediction to `difficulty_logs` table and use it for levels 6 to 10.

---

## 📌 4. Backend Developer Action Checklist

- [ ] **Step 1**: Add `patientLinkCode`, `coins`, `hintsCount`, `skipLevelCount`, `phone`, `gender`, `age`, `avatarUri` to `User.java`.
- [ ] **Step 2**: Auto-generate unique `SM-XXXX` code on `POST /auth/register` for Patient accounts.
- [ ] **Step 3**: Implement `POST /caregiver/link-by-code` and `GET /caregiver/patient/summary`.
- [ ] **Step 4**: Implement `GET /shop/items`, `POST /shop/buy`, and `POST /user/perks/use`.
- [ ] **Step 5**: Implement `POST /auth/change-password`.
- [ ] **Step 6**: Update `LevelAttemptRequest` to accept `timeTakenMs`, `idleHintsCount`, `perkHintsCount`.
- [ ] **Step 7**: Configure `WebClient` / `RestTemplate` service to call AI model on port `8000` on every 5th level.
- [ ] **Step 8**: Award **+15 XP** and **+200 Coins** in `GameService.processLevelAttempt`.
