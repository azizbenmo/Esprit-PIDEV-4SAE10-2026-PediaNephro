package com.pedianephro.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjustmentProposalResponse {
    private Long userId;

    private Long planActuelId;
    private String planActuelName;
    private Double planActuelPrice;

    private Long planRecommandeId;
    private String planRecommandeName;
    private Double planRecommandePrice;

    private AdjustmentType typeAjustement;
    private Double difference;

    private String justification;
    private Double confidenceScore;
    private LocalDateTime checkDate;
}
