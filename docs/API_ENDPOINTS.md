# SmritiSetu Demo API Contract

This document describes the Spring Boot demo backend for the React Native frontend.

## Base URL

```text
http://localhost:8080
```

For every protected request, send the JWT received from login or registration:

```http
Authorization: Bearer <token>
```

> In Postman, choose **Authorization → Bearer Token** and paste only the token value. Do not manually add a second `Authorization` header.

## Roles

| Role | Allowed demo features |
|---|---|
| `PATIENT` | Profile, games, level attempts, content, XP/league |
| `CAREGIVER` | Link one patient, dashboard, AI explanation logs, reminders |

The demo supports **one caregiver linked to one patient**. A patient cannot be linked to a second caregiver.

## Error responses

| Status | Meaning |
|---|---|
| `400` | Validation issue, invalid ID, or invalid request data |
| `401` | Missing/expired/invalid JWT |
| `403` | JWT belongs to a role without access to that endpoint |
| `409` | Duplicate username or patient already linked to another caregiver |

Errors use this shape where applicable:

```json
{ "error": "Description of the issue" }
```

---

## 1. Authentication

### Register

```http
POST /auth/register
Content-Type: application/json
```

```json
{
  "username": "patient1",
  "password": "Password123!",
  "name": "Demo Patient",
  "role": "PATIENT",
  "preferredLanguage": "en"
}
```

`role` is `PATIENT` or `CAREGIVER`. It defaults to `PATIENT`. Supported languages are `en`, `hi`, and `as`.

Response (`200`):

```json
{
  "token": "eyJ...",
  "userId": "c7b840e3-2a66-4ca3-98b4-4c96883850be",
  "role": "PATIENT"
}
```

### Login

```http
POST /auth/login
Content-Type: application/json
```

```json
{
  "username": "patient1",
  "password": "Password123!"
}
```

Returns the same response as registration. Store `token`, `userId`, and `role` securely on the device. The demo JWT expires after 30 minutes.

### Google login (optional/not configured by default)

```http
POST /auth/google
Content-Type: application/json
```

```json
{
  "idToken": "GOOGLE_ID_TOKEN",
  "role": "PATIENT"
}
```

This endpoint returns `503` until the backend has a `GOOGLE_CLIENT_ID` environment variable.

---

## 2. Patient profile

### Get profile

```http
GET /user/profile
Authorization: Bearer <patient-or-caregiver-token>
```

Response (`200`):

```json
{
  "id": "c7b840e3-2a66-4ca3-98b4-4c96883850be",
  "username": "patient1",
  "name": "Demo Patient",
  "role": "PATIENT",
  "preferredLanguage": "en"
}
```

### Update profile

```http
PUT /user/profile
Authorization: Bearer <patient-or-caregiver-token>
Content-Type: application/json
```

```json
{
  "name": "Demo Patient",
  "preferredLanguage": "as"
}
```

---

## 3. Games and content (PATIENT only)

### List available games

```http
GET /game
Authorization: Bearer <patient-token>
```

Response (`200`):

```json
[
  {
    "id": "GAME_UUID",
    "name": "Memory Match",
    "type": "memory_match"
  },
  {
    "id": "GAME_UUID",
    "name": "Sequence Recall",
    "type": "sequence_recall"
  },
  {
    "id": "GAME_UUID",
    "name": "Daily Reasoning",
    "type": "daily_reasoning"
  }
]
```

Save the selected game's `id` as `gameId` for all later game requests.

### Start current level

```http
GET /game/{gameId}/level/{level}/start
Authorization: Bearer <patient-token>
```

Example:

```text
GET /game/GAME_UUID/level/1/start
```

The frontend must request the `level` returned by the previous completion response. It cannot skip levels.

Response (`200`):

```json
{
  "gameId": "GAME_UUID",
  "gameType": "memory_match",
  "level": 1,
  "difficulty": 1,
  "language": "en",
  "levelConfig": {
    "itemCount": 3,
    "difficulty": 1,
    "timeLimitSeconds": 0,
    "audioPrompt": "Find the matching familiar pictures."
  }
}
```

`timeLimitSeconds` is intentionally `0`: the demo must not use pressure timers for elderly users.

### Get localized prompt content

```http
GET /content/{lang}/{gameId}
Authorization: Bearer <patient-token>
```

`lang`: `en`, `hi`, or `as`.

Response (`200`):

```json
{
  "language": "as",
  "gameId": "GAME_UUID",
  "content": {
    "prompt": "একে ধৰণৰ চিনাকি ছবিবোৰ বিচাৰক।",
    "type": "memory_match"
  }
}
```

The mobile app should send `content.prompt` to its on-device text-to-speech engine.

### Complete one level

```http
POST /game/level/complete
Authorization: Bearer <patient-token>
Content-Type: application/json
```

```json
{
  "gameId": "GAME_UUID",
  "timeTakenSec": 25,
  "hintsUsed": 0,
  "triesCount": 1,
  "syncedOffline": false
}
```

| Field | Rule |
|---|---|
| `gameId` | Required UUID from `GET /game` |
| `timeTakenSec` | Required integer, 0 or greater |
| `hintsUsed` | Required integer, 0 or greater |
| `triesCount` | Required integer, 1 or greater |
| `syncedOffline` | Optional boolean; send `true` for a queued offline attempt |

Response (`200`):

```json
{
  "xpEarned": 10,
  "newDifficulty": 1,
  "nextLevel": 2,
  "newLeague": "BRONZE",
  "aiReasoningMessage": null
}
```

Every fifth completed level, `aiReasoningMessage` contains the plain-language reason for the bounded difficulty decision. Difficulty can change only by one step and stays between 1 and 10.

### Sync buffered offline attempts

```http
POST /game/level/complete-bulk
Authorization: Bearer <patient-token>
Content-Type: application/json
```

The body **must be a JSON array**, even when it has one item:

```json
[
  {
    "gameId": "GAME_UUID",
    "timeTakenSec": 45,
    "hintsUsed": 1,
    "triesCount": 1,
    "syncedOffline": true
  }
]
```

Response: an array of the same completion response described above.

> Demo limitation: the app should queue attempts locally when offline and submit them in their original order once connected. Out-of-order conflict reconciliation is not included in the demo backend.

---

## 4. League / XP (PATIENT only)

### Get league status

```http
GET /league/status
Authorization: Bearer <patient-token>
```

Response (`200`):

```json
{
  "currentLeague": "BRONZE",
  "totalXp": 50,
  "xpToNextLeague": 451
}
```

Leagues promote only: `BRONZE` → `SILVER` → `GOLD` → `PLATINUM`. The demo does not demote patients.

---

## 5. Caregiver dashboard (CAREGIVER only)

### Link a patient

```http
POST /caregiver/patient/{patientId}/link
Authorization: Bearer <caregiver-token>
```

Response: `204 No Content`.

`patientId` is the patient `userId` received from patient registration/login. Link this account before requesting any patient data.

### Get patient progress

```http
GET /caregiver/patient/{patientId}/progress
Authorization: Bearer <caregiver-token>
```

Response (`200`):

```json
{
  "patientName": "Demo Patient",
  "currentLeague": "BRONZE",
  "totalXp": 50,
  "gameProgress": [
    {
      "gameName": "Memory Match",
      "currentLevel": 6,
      "currentDifficulty": 2,
      "lastPlayed": "2026-08-29T13:00:00"
    }
  ]
}
```

### Get AI difficulty reasoning

```http
GET /caregiver/patient/{patientId}/difficulty-log
Authorization: Bearer <caregiver-token>
```

Response (`200`):

```json
[
  {
    "gameName": "Memory Match",
    "aiDecision": "INCREASE",
    "reasoningText": "Patient completed tasks very quickly ...",
    "loggedAt": "2026-08-29T13:00:00"
  }
]
```

Show `reasoningText` directly in the caregiver UI as the answer to “Why did difficulty change?”

---

## 6. Reminders (CAREGIVER only)

Reminder types: `medicine`, `hydration`, `activity`, `appointment`.

### List reminders

```http
GET /caregiver/patient/{patientId}/reminders
Authorization: Bearer <caregiver-token>
```

### Create reminder

```http
POST /caregiver/patient/{patientId}/reminders
Authorization: Bearer <caregiver-token>
Content-Type: application/json
```

```json
{
  "type": "medicine",
  "scheduledTime": "08:00 AM",
  "message": "Time to take your medicine.",
  "active": true
}
```

Response (`200`):

```json
{
  "id": "REMINDER_UUID",
  "type": "medicine",
  "scheduledTime": "08:00 AM",
  "message": "Time to take your medicine.",
  "active": true
}
```

### Update reminder

```http
PUT /caregiver/patient/{patientId}/reminders/{reminderId}
Authorization: Bearer <caregiver-token>
Content-Type: application/json
```

```json
{
  "type": "medicine",
  "scheduledTime": "09:00 AM",
  "message": "Updated medicine reminder.",
  "active": false
}
```

The backend stores reminder data. Scheduling the actual notification is handled locally by the React Native device so reminders work with poor connectivity.

---

## Suggested frontend flow

1. Register/login and persist token + role + userId.
2. For patients: call `GET /game`, start the chosen level, play locally, then call `POST /game/level/complete`.
3. When offline: queue the same completion payload locally with `syncedOffline: true`; later send the queued array to `complete-bulk`.
4. For caregivers: link the patient once, then load progress, difficulty logs, and reminders.
5. Use the device-native TTS engine to read the `audioPrompt` / localized `prompt`; the demo backend does not process voice input.
