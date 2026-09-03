from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from schemas import (
    PredictionRequest,
    PredictionResponse,
    BatchPredictionRequest,
    BatchPredictionResponse
)
from predictor import DifficultyPredictor

app = FastAPI(
    title="SmritiSetu Cognitive Difficulty Predictor API",
    description="Adaptive AI Cognitive Difficulty Prediction Microservice for Dementia & Elder Memory Gaming",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Enable CORS for backend & client integration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global Predictor Instance
predictor = DifficultyPredictor()


@app.get("/health", tags=["System"])
def health_check():
    """
    Health check endpoint to verify microservice status and model readiness.
    """
    return {
        "status": "healthy",
        "service": "smritisetu-ai-difficulty-predictor",
        "model_loaded": predictor.model is not None,
        "version": "1.0.0"
    }


@app.post(
    "/predict_difficulty",
    response_model=PredictionResponse,
    status_code=status.HTTP_200_OK,
    tags=["Prediction"]
)
def predict_difficulty(request: PredictionRequest):
    """
    Evaluates player's telemetry from the last 5 levels (time taken, tries, manual hints,
    inactivity idle hints) and predicts the optimal difficulty (EASY, NORMAL, HARD) for the next 5 levels.
    """
    try:
        response = predictor.predict(request)
        return response
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to compute difficulty prediction: {str(e)}"
        )


@app.post(
    "/batch_predict",
    response_model=BatchPredictionResponse,
    status_code=status.HTTP_200_OK,
    tags=["Prediction"]
)
def batch_predict(batch_request: BatchPredictionRequest):
    """
    Batch prediction endpoint to evaluate multiple players or session blocks simultaneously.
    """
    try:
        results = [predictor.predict(req) for req in batch_request.players]
        return BatchPredictionResponse(predictions=results)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Batch prediction error: {str(e)}"
        )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
