import os
import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import StratifiedKFold, cross_val_score, train_test_split
from sklearn.metrics import classification_report, confusion_matrix
from features import FEATURE_NAMES


def generate_synthetic_dataset(num_samples: int = 3000, random_state: int = 42) -> pd.DataFrame:
    """
    Generates realistic clinical cognitive gaming telemetry datasets based on
    geriatric cognitive assessment benchmarks (MoCA / MMSE response latencies).
    """
    np.random.seed(random_state)
    records = []

    for _ in range(num_samples):
        # 3 archetypes: 0 = Needs Easy (40%), 1 = Stable Normal (40%), 2 = Needs Hard (20%)
        archetype = np.random.choice(["EASY", "NORMAL", "HARD"], p=[0.38, 0.42, 0.20])
        age = np.random.normal(72, 8)

        if archetype == "EASY":
            # High hesitation, high tries, slow reaction, fatigue slope
            avg_time = np.random.normal(75.0, 18.0)
            avg_tries = np.random.normal(3.4, 0.8)
            idle_hints_per_lvl = np.random.exponential(1.8) + 0.5
            total_idle = idle_hints_per_lvl * 5.0 + np.random.normal(0, 1.0)
            total_perk = np.random.poisson(1.2)
            time_slope = np.random.normal(4.5, 3.0)  # slowing down
            tries_slope = np.random.normal(0.4, 0.3)
            hesitation_idx = np.random.normal(1.8, 0.6)

        elif archetype == "NORMAL":
            # Moderate response, occasional idle hint, stable slope
            avg_time = np.random.normal(42.0, 10.0)
            avg_tries = np.random.normal(2.1, 0.4)
            idle_hints_per_lvl = np.random.exponential(0.4)
            total_idle = idle_hints_per_lvl * 5.0
            total_perk = np.random.poisson(0.4)
            time_slope = np.random.normal(0.2, 1.8)
            tries_slope = np.random.normal(0.0, 0.2)
            hesitation_idx = np.random.normal(0.6, 0.3)

        else:  # HARD
            # Agile, rapid matching, 0 idle hints, learning acceleration
            avg_time = np.random.normal(20.0, 5.0)
            avg_tries = np.random.normal(1.3, 0.25)
            idle_hints_per_lvl = max(0.0, np.random.normal(0.05, 0.1))
            total_idle = idle_hints_per_lvl * 5.0
            total_perk = 0.0
            time_slope = np.random.normal(-1.5, 1.0)  # speeding up
            tries_slope = np.random.normal(-0.1, 0.1)
            hesitation_idx = max(0.0, np.random.normal(0.1, 0.1))

        # Enforce realistic physiological bounds
        avg_time = max(8.0, avg_time)
        avg_tries = max(1.0, avg_tries)
        total_idle = max(0.0, total_idle)
        total_perk = max(0.0, total_perk)
        idle_hints_per_lvl = max(0.0, idle_hints_per_lvl)
        hesitation_idx = max(0.0, hesitation_idx)
        age_norm = (age - 70.0) / 15.0

        records.append({
            "avg_time_sec": avg_time,
            "avg_tries_per_pair": avg_tries,
            "total_idle_hints": total_idle,
            "total_perk_hints": total_perk,
            "idle_hints_per_level": idle_hints_per_lvl,
            "time_slope": time_slope,
            "tries_slope": tries_slope,
            "hesitation_index": hesitation_idx,
            "age_normalized": age_norm,
            "target_difficulty": archetype
        })

    return pd.DataFrame(records)


def train_and_export_model(artifact_dir: str = "artifacts") -> Pipeline:
    print("[SmritiSetu-AI] Generating synthetic geriatric cognitive telemetry dataset...")
    df = generate_synthetic_dataset(num_samples=3500)

    X = df[FEATURE_NAMES].values
    y = df["target_difficulty"].values

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )

    print(f"[SmritiSetu-AI] Training samples: {len(X_train)}, Test samples: {len(X_test)}")

    # Model Pipeline: StandardScaler + Tuned Random Forest Classifier
    pipeline = Pipeline([
        ("scaler", StandardScaler()),
        ("classifier", RandomForestClassifier(
            n_estimators=150,
            max_depth=8,
            min_samples_split=4,
            min_samples_leaf=2,
            random_state=42,
            class_weight="balanced"
        ))
    ])

    # 5-Fold Stratified Cross Validation
    cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=42)
    cv_scores = cross_val_score(pipeline, X_train, y_train, cv=cv, scoring="f1_macro")
    print(f"[SmritiSetu-AI] 5-Fold CV Macro F1: {cv_scores.mean():.4f} (+/- {cv_scores.std():.4f})")

    # Fit final pipeline
    pipeline.fit(X_train, y_train)

    # Evaluate on held-out test set
    y_pred = pipeline.predict(X_test)
    print("\n[SmritiSetu-AI] Classification Report on Test Set:")
    print(classification_report(y_test, y_pred, digits=4))

    print("[SmritiSetu-AI] Confusion Matrix:")
    print(confusion_matrix(y_test, y_pred, labels=["EASY", "NORMAL", "HARD"]))

    # Save artifact
    os.makedirs(artifact_dir, exist_ok=True)
    artifact_path = os.path.join(artifact_dir, "difficulty_predictor.joblib")
    joblib.dump(pipeline, artifact_path)
    print(f"\n[SmritiSetu-AI] Model artifact successfully saved to: {artifact_path}")

    return pipeline


if __name__ == "__main__":
    current_dir = os.path.dirname(os.path.abspath(__file__))
    artifacts_path = os.path.join(current_dir, "artifacts")
    train_and_export_model(artifact_dir=artifacts_path)
