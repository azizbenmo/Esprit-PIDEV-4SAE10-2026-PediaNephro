package com.example.consultation_microservice.services;

import com.example.consultation_microservice.entities.Consultation;
import com.example.consultation_microservice.entities.NiveauUrgence;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TriageService {

    // Mots-clés par niveau de gravité
    private static final List<String> MOTS_URGENTS = List.of(
            "anurie", "oligurie", "convulsion", "oedème pulmonaire",
            "hyperkaliémie", "insuffisance rénale aiguë", "dialyse urgente",
            "hypertension sévère", "hématurie massive", "détresse respiratoire"
    );

    private static final List<String> MOTS_PRIORITAIRES = List.of(
            "protéinurie", "créatinine élevée", "oedème", "hypertension",
            "syndrome néphrotique", "infection urinaire", "fièvre",
            "hématurie", "albuminurie", "récidive"
    );

   /* public Consultation calculerTriage(Consultation consultation) {
        int score = 0;
        List<String> justifications = new ArrayList<>();

        String texteAnalyse = buildTexteAnalyse(consultation);

        // ── Analyse des symptômes urgents ──────────────────────────────
        for (String mot : MOTS_URGENTS) {
            if (texteAnalyse.contains(mot.toLowerCase())) {
                score += 30;
                justifications.add("Symptôme critique détecté : " + mot);
            }
        }

        // ── Analyse des symptômes prioritaires ─────────────────────────
        for (String mot : MOTS_PRIORITAIRES) {
            if (texteAnalyse.contains(mot.toLowerCase())) {
                score += 15;
                justifications.add("Symptôme notable : " + mot);
            }
        }

        // ── Bonus antécédents ──────────────────────────────────────────
        if (consultation.getAntecedents() != null && !consultation.getAntecedents().isBlank()) {
            score += 10;
            justifications.add("Antécédents médicaux déclarés");
        }

        // ── Bonus résultats biologiques ────────────────────────────────
        if (consultation.getResultatsBio() != null && !consultation.getResultatsBio().isBlank()) {
            score += 10;
            justifications.add("Résultats biologiques anormaux signalés");
        }

        // ── Calcul niveau ──────────────────────────────────────────────
        score = Math.min(score, 100); // cap à 100

        NiveauUrgence niveau;
        if (score >= 60) {
            niveau = NiveauUrgence.URGENTE;
        } else if (score >= 25) {
            niveau = NiveauUrgence.PRIORITAIRE;
        } else {
            niveau = NiveauUrgence.NORMALE;
            justifications.add("Aucun indicateur d'urgence détecté");
        }

        consultation.setScoreUrgence(score);
        consultation.setNiveauUrgence(niveau);
        consultation.setJustificationTriage(
                justifications.isEmpty() ? "Consultation de routine" : String.join(" | ", justifications)
        );

        return consultation;
    }*/

    public Consultation calculerTriage(Consultation consultation) {
        int scoreSymptomes = 0;
        int scoreAntecedents = 0;
        int scoreBio = 0;
        List<String> justifications = new ArrayList<>();

        String texteAnalyse = buildTexteAnalyse(consultation);

        // ── Symptômes urgents (max 40 pts) ────────────────────────────
        for (String mot : MOTS_URGENTS) {
            if (texteAnalyse.contains(mot.toLowerCase())) {
                scoreSymptomes = Math.min(scoreSymptomes + 30, 40);
                justifications.add("Symptôme critique : " + mot);
            }
        }

        // ── Symptômes prioritaires (max 30 pts) ───────────────────────
        for (String mot : MOTS_PRIORITAIRES) {
            if (texteAnalyse.contains(mot.toLowerCase())) {
                scoreSymptomes = Math.min(scoreSymptomes + 10,
                        scoreSymptomes > 0 && MOTS_URGENTS.stream()
                                .anyMatch(u -> texteAnalyse.contains(u.toLowerCase()))
                                ? 40 : 30);
                justifications.add("Symptôme notable : " + mot);
            }
        }

        // ── Antécédents (max 15 pts) ──────────────────────────────────
        if (consultation.getAntecedents() != null && !consultation.getAntecedents().isBlank()) {
            scoreAntecedents = 15;
            justifications.add("Antécédents médicaux déclarés");
        }

        // ── Résultats biologiques (max 15 pts) ────────────────────────
        if (consultation.getResultatsBio() != null && !consultation.getResultatsBio().isBlank()) {
            scoreBio = 15;
            justifications.add("Résultats biologiques anormaux signalés");
        }

        int score = Math.min(scoreSymptomes + scoreAntecedents + scoreBio, 100);

        // ── Niveau ────────────────────────────────────────────────────
        NiveauUrgence niveau;
        if (score >= 70) {
            niveau = NiveauUrgence.URGENTE;
        } else if (score >= 30) {
            niveau = NiveauUrgence.PRIORITAIRE;
        } else {
            niveau = NiveauUrgence.NORMALE;
            justifications.add("Aucun indicateur d'urgence détecté");
        }

        consultation.setScoreUrgence(score);
        consultation.setNiveauUrgence(niveau);
        consultation.setJustificationTriage(
                justifications.isEmpty() ? "Consultation de routine" : String.join(" | ", justifications)
        );

        return consultation;
    }

    private String buildTexteAnalyse(Consultation c) {
        StringBuilder sb = new StringBuilder();
        if (c.getMotif() != null)        sb.append(c.getMotif().toLowerCase()).append(" ");
        if (c.getSymptomes() != null)    sb.append(c.getSymptomes().toLowerCase()).append(" ");
        if (c.getAntecedents() != null)  sb.append(c.getAntecedents().toLowerCase()).append(" ");
        if (c.getResultatsBio() != null) sb.append(c.getResultatsBio().toLowerCase()).append(" ");
        return sb.toString();
    }
}