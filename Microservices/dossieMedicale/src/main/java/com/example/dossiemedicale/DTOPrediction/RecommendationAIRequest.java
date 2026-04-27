package com.example.dossiemedicale.DTOPrediction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RecommendationAIRequest {

    @JsonProperty("dossier_id")
    private Long dossierId;

    @JsonProperty("temperature_max")
    private Double temperatureMax;

    @JsonProperty("saturation_min")
    private Double saturationMin;

    @JsonProperty("frequence_respiratoire_min")
    private Double frequenceRespiratoireMin;

    @JsonProperty("pouls_last")
    private Double poulsLast;

    @JsonProperty("prediction_temperature_next")
    private Double predictionTemperatureNext;

    @JsonProperty("prediction_pouls_next")
    private Double predictionPoulsNext;

    @JsonProperty("has_imagerie")
    private boolean hasImagerie;

    @JsonProperty("alert_count")
    private Integer alertCount;

    @JsonProperty("examens_existants")
    private List<String> examensExistants;
}