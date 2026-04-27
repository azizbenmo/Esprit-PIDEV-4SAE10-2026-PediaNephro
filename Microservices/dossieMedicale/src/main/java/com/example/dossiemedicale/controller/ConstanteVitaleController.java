package com.example.dossiemedicale.controller;

import com.example.dossiemedicale.entities.ConstantePrediction;
import com.example.dossiemedicale.entities.ConstanteVitale;
import com.example.dossiemedicale.services.ConstanteVitaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/constantes-vitales")
@RequiredArgsConstructor
public class ConstanteVitaleController {

    private final ConstanteVitaleService constanteVitaleService;

    @PostMapping("/ajouter")
    public ResponseEntity<ConstanteVitale> ajouterConstanteVitale(
            @RequestBody ConstanteVitale constanteVitale) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(constanteVitaleService.ajouterConstanteVitale(constanteVitale));
    }

    @PutMapping("/modifier/{id}")
    public ResponseEntity<ConstanteVitale> modifierConstanteVitale(
            @PathVariable Long id,
            @RequestBody ConstanteVitale constanteVitale) {

        return ResponseEntity.ok(constanteVitaleService.modifierConstanteVitale(id, constanteVitale));
    }

    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<Void> supprimerConstanteVitale(@PathVariable Long id) {
        constanteVitaleService.supprimerConstanteVitale(id);
        return ResponseEntity.noContent().build();
    }

    // ⚠ éviter conflit URL
    @GetMapping("/id/{id}")
    public ResponseEntity<ConstanteVitale> getConstanteVitaleById(@PathVariable Long id) {
        return ResponseEntity.ok(constanteVitaleService.getConstanteVitaleById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ConstanteVitale>> getAllConstantesVitales() {
        return ResponseEntity.ok(constanteVitaleService.getAllConstantesVitales());
    }

    // 🔵 Toutes les prédictions
    @GetMapping("/predictions/{idDossier}")
    public ResponseEntity<List<ConstantePrediction>> getPredictions(
            @PathVariable Long idDossier) {

        return ResponseEntity.ok(constanteVitaleService.getPredictionsByDossier(idDossier));
    }

    // 🔵 Prédictions par type
    @GetMapping("/predictions/{idDossier}/{type}")
    public ResponseEntity<List<ConstantePrediction>> getPredictionsByType(
            @PathVariable Long idDossier,
            @PathVariable String type) {

        return ResponseEntity.ok(constanteVitaleService
                .getPredictionsByDossierAndType(idDossier, type));
    }
}
