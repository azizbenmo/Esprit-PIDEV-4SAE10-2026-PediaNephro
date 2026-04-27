package com.example.dossiemedicale.services;

import com.example.dossiemedicale.DTOPrediction.RecommendationAIRequest;
import com.example.dossiemedicale.DTOPrediction.RecommendationAIResponse;
import com.example.dossiemedicale.entities.Alerte;
import com.example.dossiemedicale.entities.ConstantePrediction;
import com.example.dossiemedicale.entities.ConstanteVitale;
import com.example.dossiemedicale.entities.Examen;
import com.example.dossiemedicale.entities.ImagerieMedicale;
import com.example.dossiemedicale.entities.RecommandationSuivi;
import com.example.dossiemedicale.repositoories.AlerteRepository;
import com.example.dossiemedicale.repositoories.ConstantePredictionRepository;
import com.example.dossiemedicale.repositoories.ConstanteVitaleRepository;
import com.example.dossiemedicale.repositoories.ExamenRepository;
import com.example.dossiemedicale.repositoories.ImagerieMedicaleRepository;
import com.example.dossiemedicale.repositoories.RecommandationSuiviRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIRecommendationService {

    private final ConstanteVitaleRepository constanteVitaleRepository;
    private final ConstantePredictionRepository constantePredictionRepository;
    private final ExamenRepository examenRepository;
    private final ImagerieMedicaleRepository imagerieMedicaleRepository;
    private final AlerteRepository alerteRepository;
    private final RecommandationSuiviRepository recommandationSuiviRepository;
    private final RestTemplate restTemplate;

    public RecommandationSuivi genererAvecIA(Long dossierId) {
        String fastApiUrl = "http://localhost:8000/predict_recommendation";

        try {
            List<ConstanteVitale> constantes = Optional.ofNullable(
                    constanteVitaleRepository.findByDossier_IdDossierOrderByDateMesureDesc(dossierId)
            ).orElse(List.of());

            List<ConstantePrediction> predictions = Optional.ofNullable(
                    constantePredictionRepository.findByDossierIdDossierOrderByDatePredictionAsc(dossierId)
            ).orElse(List.of());

            List<Examen> examens = Optional.ofNullable(
                    examenRepository.findByDossier_IdDossierOrderByDateExamenDesc(dossierId)
            ).orElse(List.of());

            List<ImagerieMedicale> imageries = Optional.ofNullable(
                    imagerieMedicaleRepository.findByDossier_IdDossierOrderByDateExamenDesc(dossierId)
            ).orElse(List.of());

            List<Alerte> alertes = Optional.ofNullable(
                    alerteRepository.findByConstante_Dossier_IdDossierOrderByDateDeclenchementDesc(dossierId)
            ).orElse(List.of());

            RecommendationAIRequest request = new RecommendationAIRequest();
            request.setDossierId(dossierId);
            request.setTemperatureMax(getMaxByType(constantes, "TEMPERATURE"));
            request.setSaturationMin(getMinByType(constantes, "SATURATION_OXYGENE"));
            request.setFrequenceRespiratoireMin(getMinByType(constantes, "FREQUENCE_RESPIRATOIRE"));
            request.setPoulsLast(getLastByType(constantes, "POULS"));
            request.setPredictionTemperatureNext(getLastPredictionByType(predictions, "TEMPERATURE"));
            request.setPredictionPoulsNext(getLastPredictionByType(predictions, "POULS"));
            request.setHasImagerie(!imageries.isEmpty());
            request.setAlertCount(alertes.size());
            request.setExamensExistants(
                    examens.stream()
                            .map(Examen::getType)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList())
            );

            RecommendationAIResponse response =
                    restTemplate.postForObject(fastApiUrl, request, RecommendationAIResponse.class);

            if (response == null) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Réponse IA vide"
                );
            }

            RecommandationSuivi reco = new RecommandationSuivi();
            reco.setDossierId(dossierId);
            reco.setSpecialite(response.getSpecialite());
            reco.setExamensRecommandes(
                    response.getExamensRecommandes() == null
                            ? ""
                            : String.join(", ", response.getExamensRecommandes())
            );
            reco.setRappelControle(response.getRappelControle());
            reco.setConseils("IA (" + response.getConfidence() + ") : " + response.getReason());
            reco.setNiveauPriorite(response.getNiveauPriorite());
            reco.setDateCreation(LocalDateTime.now());
            reco.setSource("AI_MODEL");

            log.info("Recommandation IA générée pour dossier {} - priorité {}", dossierId, response.getNiveauPriorite());

            return recommandationSuiviRepository.save(reco);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erreur appel FastAPI recommandation IA", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur communication service IA de recommandation"
            );
        }
    }

    private Double getMaxByType(List<ConstanteVitale> constantes, String type) {
        return constantes.stream()
                .filter(c -> c.getType() != null && c.getType().toString().equalsIgnoreCase(type))
                .map(ConstanteVitale::getValeur)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(null);
    }

    private Double getMinByType(List<ConstanteVitale> constantes, String type) {
        return constantes.stream()
                .filter(c -> c.getType() != null && c.getType().toString().equalsIgnoreCase(type))
                .map(ConstanteVitale::getValeur)
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .orElse(null);
    }

    private Double getLastByType(List<ConstanteVitale> constantes, String type) {
        return constantes.stream()
                .filter(c -> c.getType() != null && c.getType().toString().equalsIgnoreCase(type))
                .map(ConstanteVitale::getValeur)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private Double getLastPredictionByType(List<ConstantePrediction> predictions, String type) {
        Double last = null;
        for (ConstantePrediction p : predictions) {
            if (p.getType() != null
                    && p.getType().toString().equalsIgnoreCase(type)
                    && p.getValeurPredite() != null) {
                last = p.getValeurPredite();
            }
        }
        return last;
    }
}