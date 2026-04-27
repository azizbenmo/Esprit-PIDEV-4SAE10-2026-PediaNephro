package com.example.dossiemedicale.controller;

import com.example.dossiemedicale.DTO.SyncRapportConsultationRequest;
import com.example.dossiemedicale.entities.RapportConsultationExterne;
import com.example.dossiemedicale.services.RapportConsultationExterneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/rapports-consultation")
@RequiredArgsConstructor
public class InternalRapportConsultationController {

    private final RapportConsultationExterneService rapportConsultationExterneService;

    @PostMapping("/sync")
    public ResponseEntity<RapportConsultationExterne> sync(@RequestBody SyncRapportConsultationRequest request) {
        RapportConsultationExterne saved = rapportConsultationExterneService.sync(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
