import pytest
from fastapi.testclient import TestClient
from schemas import (
    PredictionRequest,
    LevelTelemetry,
    DifficultyLevel
)
from features import extract_features, compute_cognitive_index, FEATURE_NAMES
from predictor import DifficultyPredictor
from app import app

client = TestClient(app)
predictor = DifficultyPredictor()


def create_sample_telemetry(times_ms, tries, idle_hints, perk_hints):
    levels = []
    for i, (t, tr, idh, ph) in enumerate(zip(times_ms, tries, idle_hints, perk_hints), 1):
        levels.append(LevelTelemetry(
            level=i,
            time_taken_ms=t,
            tries_count=tr,
            total_cards=4 + (i * 2),
            perk_hints_used=ph,
            idle_hints_triggered=idh
        ))
    return levels


class TestFeatureExtraction:
    def test_feature_vector_shape_and_names(self):
        levels = create_sample_telemetry(
            times_ms=[30000, 32000, 29000, 31000, 30000],
            tries=[3, 4, 3, 3, 4],
            idle_hints=[0, 0, 1, 0, 0],
            perk_hints=[0, 0, 0, 0, 0]
        )
        req = PredictionRequest(user_id="test_01", user_age=70, last_5_levels=levels)
        feats = extract_features(req)

        assert len(feats) == len(FEATURE_NAMES)
        assert feats[0] == pytest.approx(30.4, 0.1)  # avg time ~ 30.4s

    def test_cognitive_index_bounds(self):
        feat_dict = {
            "avg_time_sec": 25.0,
            "avg_tries_per_pair": 1.2,
            "idle_hints_per_level": 0.0,
            "time_slope": -0.5
        }
        idx = compute_cognitive_index(feat_dict)
        assert 0.0 <= idx <= 1.0
        assert idx > 0.8  # High performance should yield high cognitive index


class TestModelInference:
    def test_struggling_player_predicts_easy(self):
        # Long times (80s+), high tries (8-10), high idle hints (2-3 per level)
        levels = create_sample_telemetry(
            times_ms=[80000, 95000, 110000, 120000, 130000],
            tries=[9, 10, 12, 14, 15],
            idle_hints=[2, 3, 3, 4, 4],
            perk_hints=[1, 1, 2, 1, 2]
        )
        req = PredictionRequest(user_id="elder_struggling", user_age=76, last_5_levels=levels)
        resp = predictor.predict(req)

        assert resp.predicted_difficulty == DifficultyLevel.EASY
        assert resp.suggested_parameters.time_limit_seconds == 150
        assert resp.fatigue_detected is True

    def test_sharp_player_predicts_hard(self):
        # Rapid times (15s-20s), few tries (2-3), 0 idle hints, negative time slope
        levels = create_sample_telemetry(
            times_ms=[22000, 20000, 18000, 17000, 15000],
            tries=[2, 3, 2, 3, 2],
            idle_hints=[0, 0, 0, 0, 0],
            perk_hints=[0, 0, 0, 0, 0]
        )
        req = PredictionRequest(user_id="elder_sharp", user_age=65, last_5_levels=levels)
        resp = predictor.predict(req)

        assert resp.predicted_difficulty == DifficultyLevel.HARD
        assert resp.suggested_parameters.time_limit_seconds == 50
        assert resp.cognitive_index >= 0.70

    def test_moderate_player_predicts_normal(self):
        # Moderate times (40s-45s), normal tries (4-5), low idle hints (0-1)
        levels = create_sample_telemetry(
            times_ms=[42000, 44000, 40000, 43000, 41000],
            tries=[4, 5, 4, 5, 4],
            idle_hints=[0, 1, 0, 0, 1],
            perk_hints=[0, 0, 0, 0, 0]
        )
        req = PredictionRequest(user_id="elder_normal", user_age=68, last_5_levels=levels)
        resp = predictor.predict(req)

        assert resp.predicted_difficulty in [DifficultyLevel.NORMAL, DifficultyLevel.EASY]
        assert resp.confidence_score > 0.50


class TestApiEndpoints:
    def test_health_endpoint(self):
        response = client.get("/health")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "healthy"
        assert data["model_loaded"] is True

    def test_predict_difficulty_endpoint(self):
        payload = {
            "user_id": "test_elder_user_01",
            "user_age": 72,
            "current_difficulty": "NORMAL",
            "last_5_levels": [
                {"level": 1, "time_taken_ms": 35000, "tries_count": 4, "total_cards": 4, "perk_hints_used": 0, "idle_hints_triggered": 0},
                {"level": 2, "time_taken_ms": 38000, "tries_count": 5, "total_cards": 6, "perk_hints_used": 0, "idle_hints_triggered": 1},
                {"level": 3, "time_taken_ms": 42000, "tries_count": 6, "total_cards": 8, "perk_hints_used": 0, "idle_hints_triggered": 0},
                {"level": 4, "time_taken_ms": 40000, "tries_count": 6, "total_cards": 10, "perk_hints_used": 0, "idle_hints_triggered": 1},
                {"level": 5, "time_taken_ms": 45000, "tries_count": 7, "total_cards": 12, "perk_hints_used": 0, "idle_hints_triggered": 1}
            ]
        }
        response = client.post("/predict_difficulty", json=payload)
        assert response.status_code == 200
        data = response.json()
        assert "predicted_difficulty" in data
        assert "confidence_score" in data
        assert "suggested_parameters" in data
        assert data["suggested_parameters"]["time_limit_seconds"] in [50, 100, 150]
        assert "applied_for_levels" in data
