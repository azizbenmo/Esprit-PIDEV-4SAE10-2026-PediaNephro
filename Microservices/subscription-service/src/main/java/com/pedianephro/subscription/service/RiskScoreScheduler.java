package com.pedianephro.subscription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RiskScoreScheduler {

    private final RiskScoreService riskScoreService;

    /**
     * Chaque nuit à 02h00
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void runNightlyRiskCalculation() {
        log.info("Lancement du calcul nocturne du risque de désengagement...");
        try {
            riskScoreService.calculateAllActiveSubscriptions();
            log.info("Recalcul nocturne terminé avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors du recalcul nocturne: {}", e.getMessage());
        }
    }
}
