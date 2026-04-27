package com.example.consultation_microservice.services;

import com.example.consultation_microservice.entities.Consultation;
import com.example.consultation_microservice.entities.NiveauUrgence;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TriageServiceTest {

    private final TriageService triageService = new TriageService();

    @Test
    void calculerTriage_shouldMarkConsultationAsUrgenteWhenCriticalSignalsAccumulate() {
        Consultation consultation = new Consultation();
        consultation.setMotif("anurie");
        consultation.setSymptomes("convulsion");
        consultation.setAntecedents("syndrome nephrotique");
        consultation.setResultatsBio("creatinine elevee");

        Consultation result = triageService.calculerTriage(consultation);

        assertEquals(NiveauUrgence.URGENTE, result.getNiveauUrgence());
        assertEquals(70, result.getScoreUrgence());
        assertTrue(result.getJustificationTriage().contains("Sympt"));
    }

    @Test
    void calculerTriage_shouldMarkConsultationAsPrioritaireWhenSeveralNotableSymptomsExist() {
        Consultation consultation = new Consultation();
        consultation.setSymptomes("hypertension infection urinaire albuminurie");

        Consultation result = triageService.calculerTriage(consultation);

        assertEquals(NiveauUrgence.PRIORITAIRE, result.getNiveauUrgence());
        assertEquals(30, result.getScoreUrgence());
        assertTrue(result.getJustificationTriage().contains("hypertension"));
    }

    @Test
    void calculerTriage_shouldStayNormaleWhenNoUrgencyIndicatorIsDetected() {
        Consultation consultation = new Consultation();
        consultation.setMotif("controle de routine");
        consultation.setSymptomes("suivi general");

        Consultation result = triageService.calculerTriage(consultation);

        assertEquals(NiveauUrgence.NORMALE, result.getNiveauUrgence());
        assertEquals(0, result.getScoreUrgence());
        assertTrue(result.getJustificationTriage().contains("Aucun indicateur"));
    }
}
