package com.example.dossiemedicale.controller;

import com.example.dossiemedicale.DTO.HopitalProcheAutoResponse;
import com.example.dossiemedicale.DTO.LocalisationRequest;
import com.example.dossiemedicale.entities.LocalisationClient;
import com.example.dossiemedicale.services.GeolocalisationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/geolocalisation")
@RequiredArgsConstructor
public class GeolocalisationController {

    private final GeolocalisationService geolocalisationService;

    @PostMapping("/mettre-a-jour")
    public ResponseEntity<HopitalProcheAutoResponse> mettreAJourPositionEtTrouverHopital(
            @Valid @RequestBody LocalisationRequest request
    ) {
        return ResponseEntity.ok(
                geolocalisationService.enregistrerPositionEtTrouverHopitalProche(request)
        );
    }

    @GetMapping("/derniere-position/{enfantId}")
    public ResponseEntity<LocalisationClient> getDernierePosition(@PathVariable Long enfantId) {
        return ResponseEntity.ok(geolocalisationService.getDernierePosition(enfantId));
    }

    @GetMapping("/hopital-proche/{enfantId}")
    public ResponseEntity<HopitalProcheAutoResponse> getHopitalProcheDepuisDernierePosition(
            @PathVariable Long enfantId
    ) {
        return ResponseEntity.ok(
                geolocalisationService.getHopitalLePlusProcheDepuisDernierePosition(enfantId)
        );
    }
}