package com.example.consultation_microservice.controllers;

import com.example.consultation_microservice.dto.MedecinSyncRequest;
import com.example.consultation_microservice.entities.Medecin;
import com.example.consultation_microservice.services.MedecinSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Appelé par le microservice User lorsqu’un doctor est accepté / refusé.
 * Pas d’auth (réseau interne / gateway) — à sécuriser (API key, mTLS) en production si besoin.
 */
@RestController
@RequestMapping("/apiConsultation/internal/medecin")
public class InternalMedecinSyncController {

    @Autowired
    private MedecinSyncService medecinSyncService;

    @PostMapping("/sync")
    public ResponseEntity<Medecin> sync(@RequestBody MedecinSyncRequest body) {
        Medecin saved = medecinSyncService.upsertFromUser(body);
        if (saved == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(saved);
    }
}
