package com.example.dossiemedicale.controller;

import com.example.dossiemedicale.entities.RecommandationSuivi;
import com.example.dossiemedicale.services.AIRecommendationService;
import com.example.dossiemedicale.services.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommandations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final AIRecommendationService aiRecommendationService;

    @PostMapping("/generer/{dossierId}")
    public ResponseEntity<RecommandationSuivi> generer(@PathVariable Long dossierId) {
        return ResponseEntity.ok(recommendationService.genererPourDossier(dossierId));
    }

    @PostMapping("/generer-ia/{dossierId}")
    public ResponseEntity<RecommandationSuivi> genererIA(@PathVariable Long dossierId) {
        return ResponseEntity.ok(aiRecommendationService.genererAvecIA(dossierId));
    }

    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<RecommandationSuivi>> getByDossier(@PathVariable Long dossierId) {
        return ResponseEntity.ok(recommendationService.getHistoriqueParDossier(dossierId));
    }
}