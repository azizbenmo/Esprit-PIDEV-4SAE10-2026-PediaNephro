package com.example.dossiemedicale.services;

import com.example.dossiemedicale.entities.ConstantePrediction;
import com.example.dossiemedicale.entities.ConstanteVitale;
import com.example.dossiemedicale.entities.Examen;
import com.example.dossiemedicale.entities.ImagerieMedicale;
import com.example.dossiemedicale.entities.RecommandationSuivi;
import com.example.dossiemedicale.repositoories.ConstantePredictionRepository;
import com.example.dossiemedicale.repositoories.ConstanteVitaleRepository;
import com.example.dossiemedicale.repositoories.ExamenRepository;
import com.example.dossiemedicale.repositoories.ImagerieMedicaleRepository;
import com.example.dossiemedicale.repositoories.RecommandationSuiviRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final String PRIORITE_FAIBLE = "FAIBLE";
    private static final String PRIORITE_MOYENNE = "MOYENNE";
    private static final String PRIORITE_CRITIQUE = "CRITIQUE";

    private final ConstanteVitaleRepository constanteVitaleRepository;
    private final ConstantePredictionRepository constantePredictionRepository;
    private final ExamenRepository examenRepository;
    private final ImagerieMedicaleRepository imagerieMedicaleRepository;
    private final RecommandationSuiviRepository recommandationSuiviRepository;

    public RecommandationSuivi genererPourDossier(Long dossierId) {
        List<ConstanteVitale> constantes = Optional.ofNullable(
                constanteVitaleRepository.findByDossier_IdDossierOrderByDateMesureDesc(dossierId)
        ).orElse(Collections.emptyList());

        List<ConstantePrediction> predictions = Optional.ofNullable(
                constantePredictionRepository.findByDossierIdDossierOrderByDatePredictionAsc(dossierId)
        ).orElse(Collections.emptyList());

        List<Examen> examens = Optional.ofNullable(
                examenRepository.findByDossier_IdDossierOrderByDateExamenDesc(dossierId)
        ).orElse(Collections.emptyList());

        List<ImagerieMedicale> imageries = Optional.ofNullable(
                imagerieMedicaleRepository.findByDossier_IdDossierOrderByDateExamenDesc(dossierId)
        ).orElse(Collections.emptyList());

        Set<String> typesExamens = examens.stream()
                .map(Examen::getType)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> examensRecommandes = new ArrayList<>();
        List<String> conseils = new ArrayList<>();

        String specialite = "Pédiatre";
        String rappel = "Contrôle selon avis médical";
        String priorite = PRIORITE_FAIBLE;

        // =========================
        // 1) Analyse des constantes
        // =========================
        boolean temperatureCritique = constantes.stream()
                .anyMatch(c -> equalsType(c.getType(), "TEMPERATURE")
                        && c.getValeur() != null
                        && c.getSeuilMax() != null
                        && c.getValeur() > c.getSeuilMax());

        boolean saturationBasse = constantes.stream()
                .anyMatch(c -> equalsType(c.getType(), "SATURATION_OXYGENE")
                        && c.getValeur() != null
                        && c.getSeuilMin() != null
                        && c.getValeur() < c.getSeuilMin());

        boolean frequenceRespBasse = constantes.stream()
                .anyMatch(c -> equalsType(c.getType(), "FREQUENCE_RESPIRATOIRE")
                        && c.getValeur() != null
                        && c.getSeuilMin() != null
                        && c.getValeur() < c.getSeuilMin());

        boolean poulsAnormal = constantes.stream()
                .anyMatch(c -> equalsType(c.getType(), "POULS")
                        && c.getValeur() != null
                        && (
                        (c.getSeuilMin() != null && c.getValeur() < c.getSeuilMin()) ||
                                (c.getSeuilMax() != null && c.getValeur() > c.getSeuilMax())
                ));

        // ==========================================
        // 2) Règle urgence vitale / respiratoire
        // ==========================================
        if (temperatureCritique || saturationBasse || frequenceRespBasse) {
            specialite = "Urgences pédiatriques";
            priorite = PRIORITE_CRITIQUE;
            rappel = "Immédiat";

            addIfMissing(examensRecommandes, typesExamens, "Examen clinique");
            addIfMissing(examensRecommandes, typesExamens, "Prise de sang");

            conseils.add("Surveiller immédiatement les constantes vitales");
            conseils.add("Consulter sans délai");
            conseils.add("Assurer une surveillance rapprochée");
        }

        // ==========================================
        // 3) Détection des signes urinaires / rénaux
        // ==========================================
        boolean signeUrinaire =
                typesExamens.contains("Bandelette urinaire")
                        || typesExamens.contains("Recherche de sang dans les urines")
                        || typesExamens.contains("Recherche de protéines / albumine dans les urines")
                        || typesExamens.contains("Analyse des urines");

        if (signeUrinaire) {
            addIfMissing(examensRecommandes, typesExamens, "ECBU");
            addIfMissing(examensRecommandes, typesExamens, "Analyse des urines");
            addIfMissing(examensRecommandes, typesExamens, "Créatinine");
            addIfMissing(examensRecommandes, typesExamens, "Urée");
            addIfMissing(examensRecommandes, typesExamens, "Ionogramme sanguin");
            addIfMissing(examensRecommandes, typesExamens, "DFG / eGFR");

            // On ne change la spécialité que si le cas n'est pas déjà critique
            if (!PRIORITE_CRITIQUE.equals(priorite)) {
                specialite = "Pédiatre / Néphrologue";
                priorite = PRIORITE_MOYENNE;
                rappel = "Contrôle sous 48h";
            }

            conseils.add("Surveiller la diurèse et l’hydratation");
            conseils.add("Compléter le bilan urinaire et rénal");
        }

        // ==========================================
        // 4) Si pouls anormal sans urgence vitale
        // ==========================================
        if (poulsAnormal && PRIORITE_FAIBLE.equals(priorite)) {
            specialite = "Pédiatre";
            priorite = PRIORITE_MOYENNE;
            rappel = "Contrôle sous 24h";

            addIfMissing(examensRecommandes, typesExamens, "Examen clinique");
            conseils.add("Recontrôler le pouls et l’état général");
        }

        // ==========================================
        // 5) Exploiter les prédictions déjà en base
        // ==========================================
        boolean predictionTemperatureAnormale = predictions.stream()
                .anyMatch(p -> equalsType(p.getType(), "TEMPERATURE")
                        && p.getValeurPredite() != null
                        && p.getValeurPredite() > 38.0);

        boolean predictionPoulsAnormale = predictions.stream()
                .anyMatch(p -> equalsType(p.getType(), "POULS")
                        && p.getValeurPredite() != null
                        && (p.getValeurPredite() < 60 || p.getValeurPredite() > 100));

        if ((predictionTemperatureAnormale || predictionPoulsAnormale) && PRIORITE_FAIBLE.equals(priorite)) {
            priorite = PRIORITE_MOYENNE;
            conseils.add("Une aggravation probable est détectée selon les prédictions");
        }

        // ==========================================
        // 6) Présence d’imagerie = renforcer suivi
        // ==========================================
        if (!imageries.isEmpty()) {
            conseils.add("Tenir compte des résultats d’imagerie dans l’évaluation clinique");

            if (PRIORITE_FAIBLE.equals(priorite)) {
                priorite = PRIORITE_MOYENNE;
                rappel = "Contrôle sous 72h";
            }
        }

        // ==========================================
        // 7) Cas sans recommandation spécifique
        // ==========================================
        if (examensRecommandes.isEmpty()) {
            addIfMissing(examensRecommandes, typesExamens, "Examen clinique");
            conseils.add("Poursuivre la surveillance clinique standard");
        }

        // Nettoyage des doublons
        examensRecommandes = new ArrayList<>(new LinkedHashSet<>(examensRecommandes));
        conseils = new ArrayList<>(new LinkedHashSet<>(conseils));

        RecommandationSuivi reco = new RecommandationSuivi();
        reco.setDossierId(dossierId);
        reco.setSpecialite(specialite);
        reco.setExamensRecommandes(String.join(", ", examensRecommandes));
        reco.setRappelControle(rappel);
        reco.setConseils(String.join(" | ", conseils));
        reco.setNiveauPriorite(priorite);
        reco.setDateCreation(LocalDateTime.now());
        reco.setSource("RULE_ENGINE");

        return recommandationSuiviRepository.save(reco);
    }

    public List<RecommandationSuivi> getHistoriqueParDossier(Long dossierId) {
        return recommandationSuiviRepository.findByDossierIdOrderByDateCreationDesc(dossierId);
    }

    private void addIfMissing(List<String> examensRecommandes, Set<String> typesExamens, String examen) {
        if (!typesExamens.contains(examen) && !examensRecommandes.contains(examen)) {
            examensRecommandes.add(examen);
        }
    }

    private boolean equalsType(Object entityType, String expected) {
        if (entityType == null) {
            return false;
        }
        return entityType.toString().equalsIgnoreCase(expected);
    }
}