package com.pedianephro.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskScoreResponse {
    private Long userId;
    private Double score;
    private String riskLevel;
    private String actionRecommandee;
    private LocalDateTime calculatedAt;
    private List<RiskScoreHistoryDto> scoreHistory;
}
