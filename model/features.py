import numpy as np
from typing import List, Dict, Any
from schemas import LevelTelemetry, PredictionRequest

FEATURE_NAMES = [
    "avg_time_sec",
    "avg_tries_per_pair",
    "total_idle_hints",
    "total_perk_hints",
    "idle_hints_per_level",
    "time_slope",
    "tries_slope",
    "hesitation_index",
    "age_normalized"
]


def extract_features(request: PredictionRequest) -> np.ndarray:
    """
    Extracts a normalized 1D feature vector from the player's last played levels.
    """
    levels = request.last_5_levels
    n = len(levels)

    times_sec = np.array([lvl.time_taken_ms / 1000.0 for lvl in levels], dtype=float)
    tries = np.array([lvl.tries_count for lvl in levels], dtype=float)
    pairs = np.array([max(2, lvl.total_cards // 2) for lvl in levels], dtype=float)
    idle_hints = np.array([lvl.idle_hints_triggered for lvl in levels], dtype=float)
    perk_hints = np.array([lvl.perk_hints_used for lvl in levels], dtype=float)

    # 1. Reaction & Time metrics
    avg_time_sec = float(np.mean(times_sec))
    
    # 2. Tries efficiency: Average tries per pair (Optimal theoretical min is 1.0)
    tries_per_pair = tries / pairs
    avg_tries_per_pair = float(np.mean(tries_per_pair))

    # 3. Hint reliance
    total_idle_hints = float(np.sum(idle_hints))
    total_perk_hints = float(np.sum(perk_hints))
    idle_hints_per_level = float(np.mean(idle_hints))

    # 4. Temporal Trend (Fatigue vs Acceleration Slope over last N levels)
    if n > 1:
        x = np.arange(n)
        # Linear slope of time: positive slope means player is slowing down (fatigue)
        time_slope = float(np.polyfit(x, times_sec, 1)[0])
        # Linear slope of tries: positive means making more mistakes over time
        tries_slope = float(np.polyfit(x, tries_per_pair, 1)[0])
    else:
        time_slope = 0.0
        tries_slope = 0.0

    # 5. Inactivity Hesitation Index: idle hints per 60 seconds played
    total_time_min = max(0.1, np.sum(times_sec) / 60.0)
    hesitation_index = float(total_idle_hints / total_time_min)

    # 6. Age Context (Centered at 70)
    age = float(request.user_age if request.user_age else 68)
    age_normalized = (age - 70.0) / 15.0

    feature_vector = np.array([
        avg_time_sec,
        avg_tries_per_pair,
        total_idle_hints,
        total_perk_hints,
        idle_hints_per_level,
        time_slope,
        tries_slope,
        hesitation_index,
        age_normalized
    ], dtype=float)

    return feature_vector


def compute_cognitive_index(feature_dict: Dict[str, float]) -> float:
    """
    Calculates a continuous cognitive index (0.0 to 1.0) based on independence,
    speed, accuracy, and consistency.
    """
    avg_time = feature_dict["avg_time_sec"]
    avg_tries = feature_dict["avg_tries_per_pair"]
    idle_hints = feature_dict["idle_hints_per_level"]
    time_slope = feature_dict["time_slope"]

    # Speed component (ideal <= 25s, poor >= 90s)
    speed_score = np.clip((90.0 - avg_time) / 65.0, 0.0, 1.0)

    # Accuracy component (ideal <= 1.5 tries/pair, poor >= 4.0 tries/pair)
    accuracy_score = np.clip((4.0 - avg_tries) / 2.5, 0.0, 1.0)

    # Independence component (ideal 0 idle hints, poor >= 2.0 idle hints/level)
    independence_score = np.clip((2.0 - idle_hints) / 2.0, 0.0, 1.0)

    # Stamina component (no severe positive slowdown)
    stamina_score = 1.0 if time_slope <= 2.0 else np.clip((10.0 - time_slope) / 8.0, 0.0, 1.0)

    # Weighted aggregate cognitive index
    cognitive_index = (
        0.30 * speed_score +
        0.30 * accuracy_score +
        0.25 * independence_score +
        0.15 * stamina_score
    )

    return float(np.round(np.clip(cognitive_index, 0.05, 0.98), 3))
