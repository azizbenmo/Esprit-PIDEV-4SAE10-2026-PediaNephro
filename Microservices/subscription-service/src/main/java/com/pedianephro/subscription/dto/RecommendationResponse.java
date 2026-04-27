package com.pedianephro.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {
    private Long planId;
    private String planName;
    private Double planPrice;
    private Double confidenceScore;
    private String justification;
    private List<PlanScore> allPlansRanked;
    private List<ConseilMedical> conseilsMedicaux;
    private List<String> alertesMedicales;
}
