package com.example.dossiemedicale.DTOPrediction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RecommendationAIResponse {

    private String status;
    private String specialite;

    @JsonProperty("examens_recommandes")
    private List<String> examensRecommandes;

    @JsonProperty("rappel_controle")
    private String rappelControle;

    @JsonProperty("niveau_priorite")
    private String niveauPriorite;

    private Double confidence;
    private String reason;
}