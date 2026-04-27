package com.pedianephro.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HighRiskPatientDto {
    private Long userId;
    private String userEmail;
    private String userFullName;
    private Double score;
    private String riskLevel;
    private String actionRecommandee;
}
