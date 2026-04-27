import logging
import random
from datetime import datetime, timedelta
from collections import defaultdict
from fastapi import FastAPI
from pydantic import BaseModel

# ──────────────────────────────────────────────────────────────────────────────
# Configuration des logs
# ──────────────────────────────────────────────────────────────────────────────
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="PediaNephro AI Fraud Scanner",
    description=(
        "Moteur de détection de fraude intelligent basé sur l'analyse comportementale.\n\n"
        "## Fonctionnalités\n"
        "- **Détection de Bots** : Analyse du User-Agent (scripts, Postman, curl).\n"
        "- **Analyse de Vélocité** : Détecte les comportements anormalement rapides (brute-force, spam).\n"
        "- **Scoring Contextuel** : Évaluation selon le type d'action, l'IP et l'appareil.\n"
    ),
    version="2.0.0",
)

# ──────────────────────────────────────────────────────────────────────────────
# Mémoire d'activité des utilisateurs (Stateful In-Memory Tracking)
# Format : { user_id: [(action, timestamp), ...] }
# ──────────────────────────────────────────────────────────────────────────────
user_activity_log: dict[int, list[tuple[str, datetime]]] = defaultdict(list)

# ──────────────────────────────────────────────────────────────────────────────
# Agents / User-Agents connus comme bots ou outils automatisés
# ──────────────────────────────────────────────────────────────────────────────
BOT_KEYWORDS = [
    "postman", "curl", "python-requests", "httpie", "wget",
    "java/", "okhttp", "go-http-client", "axios/node", "insomnia",
]

# ──────────────────────────────────────────────────────────────────────────────
# Actions à risque élevé (sensitive actions)
# ──────────────────────────────────────────────────────────────────────────────
HIGH_RISK_ACTIONS = {"RECLAMATION_CREATE", "MULTIPLE_FAILED_LOGINS", "PASSWORD_CHANGE", "PAYMENT"}
MEDIUM_RISK_ACTIONS = {"LOGIN", "PROFILE_UPDATE"}

# ──────────────────────────────────────────────────────────────────────────────
# Modèles de données
# ──────────────────────────────────────────────────────────────────────────────
class FraudPredictionRequest(BaseModel):
    user_id: int | None = None
    action: str
    ip_address: str | None = None
    device_info: str | None = None
    timestamp: str | None = None

class FraudPredictionResponse(BaseModel):
    score: float
    suspicious: bool
    details: str

# ──────────────────────────────────────────────────────────────────────────────
# Logique de Nettoyage de la Mémoire (pour éviter la croissance infinie)
# ──────────────────────────────────────────────────────────────────────────────
def clean_old_events(user_id: int, window_minutes: int = 10):
    """Supprime les événements plus anciens que la fenêtre de temps."""
    cutoff = datetime.now() - timedelta(minutes=window_minutes)
    user_activity_log[user_id] = [
        (action, ts) for action, ts in user_activity_log[user_id]
        if ts > cutoff
    ]

# ──────────────────────────────────────────────────────────────────────────────
# ENDPOINT : Prédiction de Fraude
# ──────────────────────────────────────────────────────────────────────────────
@app.post("/predict", response_model=FraudPredictionResponse,
          summary="Analyser une action utilisateur",
          description="Reçoit un événement et retourne un score de fraude entre 0 (sûr) et 100 (très suspect).")
def predict_fraud(request: FraudPredictionRequest):
    logger.info(f"[AI] Analyse → Action: {request.action} | User: {request.user_id} | IP: {request.ip_address}")

    score = 0.0
    reasons = []
    action_upper = request.action.upper()

    # ──────────────────────────────────────────────────────────────────────────
    # RÈGLE 1 : Détection de Bot via User-Agent
    # ──────────────────────────────────────────────────────────────────────────
    device = (request.device_info or "").lower()
    is_bot = any(bot in device for bot in BOT_KEYWORDS)
    if is_bot:
        score += 60
        reasons.append("Agent automatisé détecté (Bot)")
        logger.warning(f"[AI] BOT détecté pour l'utilisateur {request.user_id}: '{request.device_info}'")
    elif not request.device_info or request.device_info.strip() == "":
        score += 15
        reasons.append("Appareil inconnu")

    # ──────────────────────────────────────────────────────────────────────────
    # RÈGLE 2 : Analyse de l'Adresse IP
    # ──────────────────────────────────────────────────────────────────────────
    # Toutes les formes connues du loopback local (IPv4 + IPv6 court + IPv6 long)
    LOCAL_IPS = ("127.0.0.1", "::1", "0:0:0:0:0:0:0:1")

    if not request.ip_address:
        score += 10
        reasons.append("IP manquante")
    elif request.ip_address in LOCAL_IPS:
        # IP loopback locale — considérée sûre en développement
        pass
    elif request.ip_address.startswith(("192.168.", "10.", "172.")):
        # Réseau local privé — considéré sûr dans ce contexte
        pass
    else:
        score += 20
        reasons.append(f"IP externe inconnue ({request.ip_address})")

    # ──────────────────────────────────────────────────────────────────────────
    # RÈGLE 3 : Score de Base selon le Type d'Action
    # ──────────────────────────────────────────────────────────────────────────
    if action_upper in HIGH_RISK_ACTIONS:
        score += 35
        reasons.append(f"Action à risque élevé ({request.action})")
    elif action_upper in MEDIUM_RISK_ACTIONS:
        score += 10
    else:
        # Action inconnue / non catégorisée
        score += 25
        reasons.append(f"Action non reconnue ({request.action})")

    # ──────────────────────────────────────────────────────────────────────────
    # RÈGLE 4 : Analyse de Vélocité (Comportement Rapide / Brute Force)
    # Fenêtres temporelles multiples pour une détection fine
    # ──────────────────────────────────────────────────────────────────────────
    if request.user_id is not None:
        clean_old_events(request.user_id)
        now = datetime.now()

        # Compter les actions dans les 60 dernières secondes
        last_60s = [
            (a, ts) for a, ts in user_activity_log[request.user_id]
            if ts > now - timedelta(seconds=60)
        ]
        # Compter les actions dans les 5 dernières minutes
        last_5min = [
            (a, ts) for a, ts in user_activity_log[request.user_id]
            if ts > now - timedelta(minutes=5)
        ]

        count_60s = len(last_60s)
        count_5min = len(last_5min)

        if count_60s >= 5:
            score += 50
            reasons.append(f"Fréquence anormale: {count_60s} actions en 60 secondes (Brute Force?)")
            logger.warning(f"[AI] ANOMALIE VÉLOCITÉ pour user {request.user_id}: {count_60s} actions/60s")
        elif count_60s >= 3:
            score += 25
            reasons.append(f"Activité rapide: {count_60s} actions en 60 secondes")
        elif count_5min >= 8:
            score += 20
            reasons.append(f"Activité soutenue: {count_5min} actions en 5 minutes")

        # Enregistrer l'événement actuel dans la mémoire
        user_activity_log[request.user_id].append((action_upper, now))

    # ──────────────────────────────────────────────────────────────────────────
    # RÈGLE 5 : Analyse de l'Heure (activité nocturne suspecte)
    # ──────────────────────────────────────────────────────────────────────────
    current_hour = datetime.now().hour
    if current_hour < 5 or current_hour >= 23:
        score += 15
        reasons.append(f"Activité nocturne suspecte (heure: {current_hour}h)")

    # ──────────────────────────────────────────────────────────────────────────
    # RÈGLE 6 : Login en dehors des heures de bureau
    # ──────────────────────────────────────────────────────────────────────────
    if action_upper == "LOGIN" and (current_hour < 7 or current_hour >= 22):
        score += 10
        reasons.append("Connexion hors des heures habituelles")

    # ──────────────────────────────────────────────────────────────────────────
    # Calcul Final du Score avec un bruit minimal pour simuler l'incertitude ML
    # ──────────────────────────────────────────────────────────────────────────
    noise = random.uniform(-5.0, 8.0)
    final_score = min(max(score + noise, 0.0), 100.0)

    # Seuil de décision : > 65 → Suspect
    is_suspicious = final_score > 65.0

    if not reasons:
        reasons.append("Comportement normal")
    if is_suspicious:
        reasons.append("⚠️ Seuil de fraude dépassé")

    details_str = " | ".join(reasons)
    logger.info(f"[AI] Résultat → Score={final_score:.2f} | Suspect={is_suspicious} | Raisons: {details_str}")

    return FraudPredictionResponse(
        score=round(final_score, 2),
        suspicious=is_suspicious,
        details=details_str,
    )

# ──────────────────────────────────────────────────────────────────────────────
# ENDPOINT : Santé du Service
# ──────────────────────────────────────────────────────────────────────────────
@app.get("/health", summary="Vérification de l'état du service")
def health_check():
    active_users_tracked = len(user_activity_log)
    return {
        "status": "UP",
        "version": "2.0.0",
        "timestamp": datetime.now().isoformat(),
        "active_user_sessions_in_memory": active_users_tracked,
    }

# ──────────────────────────────────────────────────────────────────────────────
# ENDPOINT ROOT
# ──────────────────────────────────────────────────────────────────────────────
@app.get("/", summary="Info du service")
def root():
    return {
        "service": "PediaNephro AI Fraud Scanner v2.0",
        "status": "opérationnel",
        "docs": "http://127.0.0.1:8000/docs",
        "health": "http://127.0.0.1:8000/health",
    }
