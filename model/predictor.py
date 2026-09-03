import os
import joblib
import numpy as np
from typing import Dict, Any, Optional
from features import extract_features, compute_cognitive_index, FEATURE_NAMES
from schemas import (
    PredictionRequest,
    PredictionResponse,
    DifficultyLevel,
    SuggestedParameters
)


class DifficultyPredictor:
    def __init__(self, model_path: Optional[str] = None):
        if model_path is None:
            current_dir = os.path.dirname(os.path.abspath(__file__))
            model_path = os.path.join(current_dir, "artifacts", "difficulty_predictor.joblib")
        
        self.model_path = model_path
        self.model = None
        self._load_model()

    def _load_model(self):
        if os.path.exists(self.model_path):
            self.model = joblib.load(self.model_path)
            print(f"[SmritiSetu-AI] Loaded difficulty prediction model from {self.model_path}")
        else:
            print(f"[SmritiSetu-AI] Model artifact not found at {self.model_path}. Training fallback...")
            from train import train_and_export_model
            artifact_dir = os.path.dirname(self.model_path)
            self.model = train_and_export_model(artifact_dir=artifact_dir)

    def predict(self, request: PredictionRequest) -> PredictionResponse:
        feature_vector = extract_features(request)
        feature_dict = dict(zip(FEATURE_NAMES, feature_vector))
        cognitive_index = compute_cognitive_index(feature_dict)

        # Model Inference
        X = feature_vector.reshape(1, -1)
        classes = list(self.model.classes_)
        probabilities = self.model.predict_proba(X)[0]
        pred_class = self.model.predict(X)[0]
        confidence = float(np.max(probabilities))

        predicted_diff = DifficultyLevel(pred_class)
        time_slope = feature_dict["time_slope"]
        avg_time = feature_dict["avg_time_sec"]
        avg_tries = feature_dict["avg_tries_per_pair"]
        idle_hints = feature_dict["total_idle_hints"]

        # Detect fatigue slope (> +3s slowdown per level)
        fatigue_detected = time_slope > 3.0 or (feature_dict["tries_slope"] > 0.35)

        # Generate Clinical / Cognitive Rationale
        rationale_points = []
        if idle_hints >= 3:
            rationale_points.append(f"Frequent hesitation detected ({int(idle_hints)} idle hint prompts triggered)")
        if avg_time > 60:
            rationale_points.append(f"Average level completion time is {avg_time:.1f}s, indicating need for calm relaxed pace")
        elif avg_time < 25:
            rationale_points.append(f"Rapid response time of {avg_time:.1f}s demonstrates high visual recall")

        if avg_tries > 3.0:
            rationale_points.append(f"Elevated mismatch rate ({avg_tries:.1f} tries/pair)")
        elif avg_tries <= 1.4:
            rationale_points.append(f"Exceptional accuracy ({avg_tries:.1f} tries/pair)")

        if fatigue_detected:
            rationale_points.append("Fatigue trend observed over the last 5 levels")

        if not rationale_points:
            rationale_points.append("Consistent, stable memory recall and interaction speed")

        rationale = "; ".join(rationale_points) + f". Assigned {predicted_diff.value} mode for optimal engagement."

        # Suggested Level Parameters
        if predicted_diff == DifficultyLevel.EASY:
            suggested_params = SuggestedParameters(
                time_limit_seconds=150,
                idle_hint_delay_seconds=5.0,
                card_pairs_start=2,
                max_card_pairs=6
            )
        elif predicted_diff == DifficultyLevel.NORMAL:
            suggested_params = SuggestedParameters(
                time_limit_seconds=100,
                idle_hint_delay_seconds=6.0,
                card_pairs_start=3,
                max_card_pairs=8
            )
        else:  # HARD
            suggested_params = SuggestedParameters(
                time_limit_seconds=50,
                idle_hint_delay_seconds=7.0,
                card_pairs_start=4,
                max_card_pairs=8
            )

        return PredictionResponse(
            user_id=request.user_id or "anonymous",
            predicted_difficulty=predicted_diff,
            confidence_score=float(np.round(confidence, 3)),
            cognitive_index=cognitive_index,
            fatigue_detected=fatigue_detected,
            suggested_parameters=suggested_params,
            rationale=rationale,
            applied_for_levels="Next 5 Levels"
        )
