package com.pedianephro.subscription.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_score_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Double score;

    @Column(name = "risk_level", nullable = false, length = 10)
    private String riskLevel;

    @Column(name = "action_recommandee", length = 500)
    private String actionRecommandee;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @PrePersist
    protected void onCreate() {
        calculatedAt = LocalDateTime.now();
    }
}
