from enum import Enum
from typing import List, Optional
from pydantic import BaseModel, Field


class DifficultyLevel(str, Enum):
    EASY = "EASY"
    NORMAL = "NORMAL"
    HARD = "HARD"


class LevelTelemetry(BaseModel):
    level: int = Field(..., ge=1, description="Level number played")
    time_taken_ms: int = Field(..., ge=100, description="Time taken to complete level in milliseconds")
    tries_count: int = Field(..., ge=1, description="Total number of card flip tries")
    total_cards: int = Field(default=4, ge=4, description="Total number of cards on the board")
    perk_hints_used: int = Field(default=0, ge=0, description="Number of manual perk hints used by player")
    idle_hints_triggered: int = Field(default=0, ge=0, description="Inactivity auto-highlight hints triggered")
    difficulty: Optional[str] = Field(default="NORMAL", description="Difficulty setting of this level")


class PredictionRequest(BaseModel):
    user_id: Optional[str] = Field(default="anonymous", description="Unique player identifier")
    user_age: Optional[int] = Field(default=68, ge=18, le=120, description="Player age in years")
    current_difficulty: Optional[DifficultyLevel] = Field(default=DifficultyLevel.NORMAL, description="Current difficulty")
    last_5_levels: List[LevelTelemetry] = Field(
        ...,
        min_length=1,
        max_length=10,
        description="Telemetry records for the most recent levels played (last 5 recommended)"
    )


class SuggestedParameters(BaseModel):
    time_limit_seconds: int = Field(..., description="Suggested time limit in seconds (150s for Easy, 100s for Normal, 50s for Hard)")
    idle_hint_delay_seconds: float = Field(..., description="Suggested inactivity hint delay in seconds")
    card_pairs_start: int = Field(..., description="Starting card pairs for upcoming block")
    max_card_pairs: int = Field(default=8, description="Maximum card pairs cap for elder readability")


class PredictionResponse(BaseModel):
    user_id: str
    predicted_difficulty: DifficultyLevel
    confidence_score: float = Field(..., ge=0.0, le=1.0, description="Model prediction confidence probability")
    cognitive_index: float = Field(..., ge=0.0, le=1.0, description="Normalized cognitive response score (higher = faster / more independent)")
    fatigue_detected: bool = Field(..., description="Flag if performance degradation / fatigue slope is detected over last 5 levels")
    suggested_parameters: SuggestedParameters
    rationale: str = Field(..., description="Detailed clinical and telemetry explanation of why this difficulty was assigned")
    applied_for_levels: str = Field(..., description="Target level range this prediction applies to (e.g. Next 5 levels)")


class BatchPredictionRequest(BaseModel):
    players: List[PredictionRequest]


class BatchPredictionResponse(BaseModel):
    predictions: List[PredictionResponse]
