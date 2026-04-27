package com.pedianephro.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskScoreHistoryDto {
    private Double score;
    private String riskLevel;
    private LocalDateTime calculatedAt;
}
