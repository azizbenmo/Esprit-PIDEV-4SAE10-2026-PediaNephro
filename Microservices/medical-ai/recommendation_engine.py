def predict_recommendation(data):
    examens_recommandes = []
    reasons = []

    specialite = "Pédiatre"
    rappel = "Contrôle selon avis médical"
    priorite = "FAIBLE"
    confidence = 0.70

    # =========================
    # 1) Urgence vitale
    # =========================
    urgence = False

    if data.temperature_max is not None and data.temperature_max > 38.0:
        urgence = True
        reasons.append("fièvre élevée")

    if data.saturation_min is not None and data.saturation_min < 95:
        urgence = True
        reasons.append("désaturation")

    if data.frequence_respiratoire_min is not None and data.frequence_respiratoire_min < 12:
        urgence = True
        reasons.append("fréquence respiratoire basse")

    if urgence:
        specialite = "Urgences pédiatriques"
        priorite = "CRITIQUE"
        rappel = "Immédiat"
        confidence = 0.92

        if "Examen clinique" not in data.examens_existants:
            examens_recommandes.append("Examen clinique")
        if "Prise de sang" not in data.examens_existants:
            examens_recommandes.append("Prise de sang")

    # =========================
    # 2) Signes urinaires / rénaux
    # =========================
    signes_urinaires = any(x in data.examens_existants for x in [
        "Bandelette urinaire",
        "Recherche de sang dans les urines",
        "Recherche de protéines / albumine dans les urines",
        "Analyse des urines"
    ])

    if signes_urinaires:
        reasons.append("signes urinaires détectés")

        for ex in [
            "ECBU",
            "Analyse des urines",
            "Créatinine",
            "Urée",
            "Ionogramme sanguin",
            "DFG / eGFR"
        ]:
            if ex not in data.examens_existants:
                examens_recommandes.append(ex)

        if priorite != "CRITIQUE":
            specialite = "Pédiatre / Néphrologue"
            priorite = "MOYENNE"
            rappel = "Contrôle sous 48h"
            confidence = max(confidence, 0.85)

    # =========================
    # 3) Prédictions futures
    # =========================
    if priorite == "FAIBLE":
        if data.prediction_temperature_next is not None and data.prediction_temperature_next > 38:
            priorite = "MOYENNE"
            reasons.append("température future estimée élevée")
            confidence = max(confidence, 0.80)

        if data.prediction_pouls_next is not None and (
            data.prediction_pouls_next < 60 or data.prediction_pouls_next > 100
        ):
            priorite = "MOYENNE"
            reasons.append("pouls futur estimé anormal")
            confidence = max(confidence, 0.80)

    # =========================
    # 4) Imagerie
    # =========================
    if data.has_imagerie:
        reasons.append("imagerie disponible")
        confidence = min(confidence + 0.02, 0.99)

    # =========================
    # 5) Nombre d'alertes
    # =========================
    if data.alert_count >= 3 and priorite == "FAIBLE":
        priorite = "MOYENNE"
        reasons.append("plusieurs alertes détectées")
        confidence = max(confidence, 0.82)

    examens_recommandes = list(dict.fromkeys(examens_recommandes))

    return {
        "specialite": specialite,
        "examens_recommandes": examens_recommandes,
        "rappel_controle": rappel,
        "niveau_priorite": priorite,
        "confidence": round(confidence, 4),
        "reason": ", ".join(reasons) if reasons else "Analyse standard du dossier"
    }