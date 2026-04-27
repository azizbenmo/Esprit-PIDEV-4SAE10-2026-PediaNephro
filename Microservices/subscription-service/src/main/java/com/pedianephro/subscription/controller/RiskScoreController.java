package com.pedianephro.subscription.controller;

import com.pedianephro.subscription.dto.*;
import com.pedianephro.subscription.service.RiskScoreService;
import com.pedianephro.subscription.repository.RiskScoreHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/risk", "/risk"})
@CrossOrigin
@RequiredArgsConstructor
public class RiskScoreController {

    private final RiskScoreService riskScoreService;
    private final RiskScoreHistoryRepository riskScoreHistoryRepository;

    /**
     * Calcule et retourne le score de risque actuel pour un utilisateur
     */
    @GetMapping("/score/{userId}")
    public ResponseEntity<RiskScoreResponse> getRiskScore(@PathVariable Long userId) {
        return ResponseEntity.ok(riskScoreService.calculateRiskScore(userId));
    }

    /**
     * Retourne les 30 derniers points d'historique de risque
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<RiskScoreHistoryDto>> getRiskHistory(@PathVariable Long userId) {
        List<RiskScoreHistoryDto> history = riskScoreHistoryRepository.findTop30ByUserIdOrderByCalculatedAtDesc(userId)
                .stream()
                .map(h -> RiskScoreHistoryDto.builder()
                        .score(h.getScore())
                        .riskLevel(h.getRiskLevel())
                        .calculatedAt(h.getCalculatedAt())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }

    /**
     * Met à jour manuellement le comportement utilisateur
     */
    @PostMapping("/behavior")
    public ResponseEntity<Void> updateBehavior(@RequestBody UserBehaviorRequest request) {
        riskScoreService.updateBehavior(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Liste des patients présentant un risque ÉLEVÉ
     */
    @GetMapping("/high-risk-patients")
    public ResponseEntity<List<HighRiskPatientDto>> getHighRiskPatients() {
        return ResponseEntity.ok(riskScoreService.getHighRiskPatients());
    }

    /**
     * Simule un comportement réaliste et retourne le nouveau score (Démo)
     */
    @PostMapping("/simulate/{userId}")
    public ResponseEntity<RiskScoreResponse> simulateBehavior(@PathVariable Long userId) {
        return ResponseEntity.ok(riskScoreService.simulateBehavior(userId));
    }
}
