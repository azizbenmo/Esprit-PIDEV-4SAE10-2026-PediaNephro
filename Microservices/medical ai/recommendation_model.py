from pydantic import BaseModel
from typing import List, Optional


class RecommendationRequest(BaseModel):
    dossier_id: int
    temperature_max: Optional[float] = None
    saturation_min: Optional[float] = None
    frequence_respiratoire_min: Optional[float] = None
    pouls_last: Optional[float] = None
    prediction_temperature_next: Optional[float] = None
    prediction_pouls_next: Optional[float] = None
    has_imagerie: bool = False
    alert_count: int = 0
    examens_existants: List[str] = []