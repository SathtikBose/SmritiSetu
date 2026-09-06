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


@app.api_route("/", methods=["GET", "HEAD", "POST"], tags=["System"])
def root():
    """
    Root endpoint for service discovery, uptime monitoring, and quick verification.
    """
    return {
        "service": "SmritiSetu Cognitive Difficulty Predictor API",
        "status": "online",
        "health_check": "/health",
        "docs": "/docs",
        "version": "1.0.0"
    }


@app.api_route("/health", methods=["GET", "HEAD", "POST"], tags=["System"])
def health_check():
    """
    Health check endpoint to verify microservice status and model readiness.
    Supports GET, HEAD, and POST for compatibility with all uptime monitors (UptimeRobot, Pingdom, etc.).
    """
    return {
        "status": "healthy",
        "service": "smritisetu-ai-difficulty-predictor",
        "model_loaded": predictor.model is not None,
        "version": "1.0.0"
    }


@app.api_route("/healthz", methods=["GET", "HEAD", "POST"], tags=["System"])
def healthz():
    """
    Kubernetes / Render / Cloud healthz probe alias.
    """
    return health_check()


@app.api_route("/ping", methods=["GET", "HEAD", "POST"], tags=["System"])
def ping():
    """
    Simple ping endpoint for uptime monitors.
    """
    return {"ping": "pong", "status": "ok"}


@app.get("/predict", tags=["Prediction"])
@app.head("/predict", tags=["Prediction"])
@app.get("/predict_difficulty", tags=["Prediction"])
@app.head("/predict_difficulty", tags=["Prediction"])
def predict_info():
    """
    Informational endpoint when GET/HEAD is sent to prediction routes (prevents 405 for monitors).
    """
    return {
        "service": "SmritiSetu Difficulty Prediction API",
        "status": "ready",
        "method": "POST",
        "message": "Send a POST request with player telemetry JSON to get difficulty prediction."
    }


@app.post(
    "/predict",
    response_model=PredictionResponse,
    status_code=status.HTTP_200_OK,
    tags=["Prediction"]
)
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
    import os
    import uvicorn
    port = int(os.environ.get("PORT", 8000))
    uvicorn.run("app:app", host="0.0.0.0", port=port, reload=False)


