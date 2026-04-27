import logging
import os
import shutil

from fastapi import FastAPI, UploadFile, File
from pydantic import BaseModel

from vision_model import predict_image
from chronos_model import predict_next_value

from recommendation_model import RecommendationRequest
from recommendation_engine import predict_recommendation
LOGGER = logging.getLogger("medical-ai-service")
logging.basicConfig(level=logging.INFO)
BASE_DIR = os.path.dirname(__file__)
PROPERTIES_PATH = os.path.join(BASE_DIR, "application.properties")
EUREKA_CLIENT_STARTED = False


def load_properties(path: str) -> dict[str, str]:
    properties: dict[str, str] = {}
    if not os.path.exists(path):
        return properties

    with open(path, "r", encoding="utf-8") as file:
        for raw_line in file:
            line = raw_line.strip()
            if not line or line.startswith(("#", "!")) or "=" not in line:
                continue
            key, value = line.split("=", 1)
            properties[key.strip()] = value.strip()
    return properties


def as_bool(value: str | None, default: bool = False) -> bool:
    if value is None:
        return default
    return value.strip().lower() in {"true", "1", "yes", "on"}


APP_PROPERTIES = load_properties(PROPERTIES_PATH)
SERVICE_NAME = os.getenv(
    "SPRING_APPLICATION_NAME",
    APP_PROPERTIES.get("spring.application.name", "medical-ai-service"),
)
SERVICE_PORT = int(os.getenv("SERVER_PORT", APP_PROPERTIES.get("server.port", "8000")))
SERVICE_HOST = os.getenv(
    "EUREKA_INSTANCE_HOST",
    APP_PROPERTIES.get("eureka.instance.hostname", "localhost"),
)
EUREKA_ENABLED = as_bool(
    os.getenv(
        "EUREKA_CLIENT_REGISTER_WITH_EUREKA",
        APP_PROPERTIES.get("eureka.client.register-with-eureka", "true"),
    ),
    default=True,
)
EUREKA_SERVER = os.getenv(
    "EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE",
    APP_PROPERTIES.get("eureka.client.service-url.defaultZone", ""),
)
if EUREKA_SERVER and not EUREKA_SERVER.endswith("/"):
    EUREKA_SERVER = f"{EUREKA_SERVER}/"
app = FastAPI(title=SERVICE_NAME)


# =========================
# ENDPOINT IMAGE
# =========================
from fastapi import HTTPException


@app.get("/health")
def health():
    return {
        "status": "UP",
        "service": SERVICE_NAME,
        "port": SERVICE_PORT,
        "eurekaEnabled": EUREKA_ENABLED,
        "eurekaServer": EUREKA_SERVER,
    }


@app.on_event("startup")
async def register_in_eureka():
    global EUREKA_CLIENT_STARTED

    if not EUREKA_ENABLED or not EUREKA_SERVER:
        LOGGER.info("Eureka registration disabled or missing server URL.")
        return

    try:
        import py_eureka_client.eureka_client as eureka_client

        await eureka_client.init_async(
            eureka_server=EUREKA_SERVER,
            should_register=True,
            should_discover=False,
            app_name=SERVICE_NAME,
            instance_host=SERVICE_HOST,
            instance_port=SERVICE_PORT,
            home_page_url=f"http://{SERVICE_HOST}:{SERVICE_PORT}/",
            status_page_url=f"http://{SERVICE_HOST}:{SERVICE_PORT}/health",
            health_check_url=f"http://{SERVICE_HOST}:{SERVICE_PORT}/health",
        )
        EUREKA_CLIENT_STARTED = True
        LOGGER.info("Registered %s in Eureka at %s", SERVICE_NAME, EUREKA_SERVER)
    except ImportError:
        LOGGER.warning(
            "py_eureka_client is not installed. Install it with: pip install py-eureka-client"
        )
    except Exception:
        LOGGER.exception("Unable to register service in Eureka.")


@app.on_event("shutdown")
async def unregister_from_eureka():
    global EUREKA_CLIENT_STARTED

    if not EUREKA_CLIENT_STARTED:
        return

    import py_eureka_client.eureka_client as eureka_client

    await eureka_client.stop_async()
    EUREKA_CLIENT_STARTED = False

@app.post("/prediction_type_fichier")
async def analyze_image(file: UploadFile = File(...)):
    # Vérifier l'extension
    if not file.filename.lower().endswith((".png", ".jpg", ".jpeg")):
        raise HTTPException(status_code=400, detail="Format non supporté. Utilisez PNG ou JPG.")

    file_path = f"temp_{file.filename}"

    try:
        # Sauvegarder temporairement
        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        prediction, confidence = predict_image(file_path)

        # Si non reconnu (Autre ou confiance faible)
        if prediction is None:
            raise HTTPException(
                status_code=400,
                detail="L'image n'est pas une radio, un scanner ou un IRM valide."
            )

        return {
            "type": prediction,
            "confidence": round(confidence, 4),
            "status": "success"
        }

    finally:
        if os.path.exists(file_path):
            os.remove(file_path)
# =========================
# ENDPOINT CONSTANTES
# =========================
class ChronosRequest(BaseModel):
    values: list[float]


@app.post("/predict_constante")
def predict_constante(data: ChronosRequest):

    next_value = predict_next_value(data.values)

    return {
        "next_value": next_value,
        "message": "Prochaine valeur prédite "
    }


# =========================
# ENDPOINT RECOMMANDATION IA
# =========================
@app.post("/predict_recommendation")
def predict_recommendation_endpoint(data: RecommendationRequest):
    result = predict_recommendation(data)
    return {
        "status": "success",
        **result
    }


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=SERVICE_PORT, reload=False)