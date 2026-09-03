# SmritiSetu Cognitive Difficulty Predictor Microservice

An adaptive Machine Learning microservice that predicts the optimal difficulty level (`EASY`, `NORMAL`, `HARD`) for dementia patients and elder memory gamers based on their previous 5 levels of cognitive telemetry.

---

## Architecture & Cognitive Modeling

The model ingests 5 consecutive levels of game telemetry and extracts a multi-dimensional cognitive response feature vector:

1. **Reaction Time Dynamics (`avg_time_sec`)**: Average completion time per level.
2. **Tries Efficiency Ratio (`avg_tries_per_pair`)**: Guess attempts relative to number of card pairs.
3. **Inactivity Hesitation (`total_idle_hints`, `idle_hints_per_level`)**: Frequency of auto-highlight hints triggered due to player inactivity.
4. **Manual Perk Reliance (`total_perk_hints`)**: Number of manual hint perks purchased/used.
5. **Cognitive Fatigue Slope (`time_slope`, `tries_slope`)**: Linear regression gradient over the 5-level window to detect cognitive exhaustion vs. learning acceleration.
6. **Hesitation Index (`hesitation_index`)**: Inactivity trigger rate per 60 seconds played.

### Target Difficulty Recommendations:
- **`EASY`**: 150s Time limit, 5.0s Inactivity hint delay, 2 to 6 card pairs.
- **`NORMAL`**: 100s Time limit, 6.0s Inactivity hint delay, 3 to 8 card pairs.
- **`HARD`**: 50s Time limit, 7.0s Inactivity hint delay, 4 to 8 card pairs.

---

## Project Structure

```
model/
├── artifacts/
│   └── difficulty_predictor.joblib  # Serialized Scikit-Learn Pipeline
├── app.py                            # FastAPI REST Microservice
├── features.py                       # Cognitive Feature Extraction Pipeline
├── predictor.py                      # Model Inference & Rationale Engine
├── schemas.py                        # Pydantic v2 Schemas
├── train.py                          # Training Pipeline & Dataset Generator
├── test_model.py                     # Pytest Unit & Integration Tests
└── requirements.txt                  # Python Dependencies
```

---

## Quickstart

### 1. Set Up Environment
```bash
python -m venv .venv
# On Windows:
.venv\Scripts\activate
# On Linux/macOS:
source .venv/bin/activate

pip install -r requirements.txt
```

### 2. Train / Retrain Model
```bash
python train.py
```

### 3. Run Unit & Integration Tests
```bash
pytest test_model.py -v
```

### 4. Start the REST API Service
```bash
python app.py
# Or with uvicorn:
uvicorn app:app --host 0.0.0.0 --port 8000 --reload
```
Interactive Swagger API documentation is available at: `http://localhost:8000/docs`

---

## REST API Specification

### `POST /predict_difficulty`

**Request Body (`application/json`)**:
```json
{
  "user_id": "user_elder_001",
  "user_age": 72,
  "current_difficulty": "NORMAL",
  "last_5_levels": [
    {
      "level": 1,
      "time_taken_ms": 32000,
      "tries_count": 4,
      "total_cards": 4,
      "perk_hints_used": 0,
      "idle_hints_triggered": 0
    },
    {
      "level": 2,
      "time_taken_ms": 36000,
      "tries_count": 5,
      "total_cards": 6,
      "perk_hints_used": 0,
      "idle_hints_triggered": 1
    },
    {
      "level": 3,
      "time_taken_ms": 41000,
      "tries_count": 6,
      "total_cards": 8,
      "perk_hints_used": 0,
      "idle_hints_triggered": 0
    },
    {
      "level": 4,
      "time_taken_ms": 39000,
      "tries_count": 6,
      "total_cards": 10,
      "perk_hints_used": 0,
      "idle_hints_triggered": 1
    },
    {
      "level": 5,
      "time_taken_ms": 44000,
      "tries_count": 7,
      "total_cards": 12,
      "perk_hints_used": 0,
      "idle_hints_triggered": 1
    }
  ]
}
```

**Response Body (`200 OK`)**:
```json
{
  "user_id": "user_elder_001",
  "predicted_difficulty": "NORMAL",
  "confidence_score": 0.89,
  "cognitive_index": 0.74,
  "fatigue_detected": false,
  "suggested_parameters": {
    "time_limit_seconds": 100,
    "idle_hint_delay_seconds": 6.0,
    "card_pairs_start": 3,
    "max_card_pairs": 8
  },
  "rationale": "Consistent, stable memory recall and interaction speed. Assigned NORMAL mode for optimal engagement.",
  "applied_for_levels": "Next 5 Levels"
}
```

### `GET /health`
Returns service status and verification that the model artifact is loaded.
