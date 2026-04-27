package com.example.dossiemedicale.controller;

import com.example.dossiemedicale.DTO.EnfantRequest;
import com.example.dossiemedicale.entities.Enfant;
import com.example.dossiemedicale.services.EnfantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enfants")
@RequiredArgsConstructor
public class EnfantController {

    private final EnfantService enfantService;

    @PostMapping("/ajouter")
    public ResponseEntity<Enfant> ajouterEnfant(@Valid @RequestBody EnfantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enfantService.ajouterEnfant(request));
    }

    @PutMapping("/modifier/{id}")
    public ResponseEntity<Enfant> modifierEnfant(
            @PathVariable Long id,
            @Valid @RequestBody EnfantRequest request
    ) {
        return ResponseEntity.ok(enfantService.modifierEnfant(id, request));
    }

    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<Void> supprimerEnfant(@PathVariable Long id) {
        enfantService.supprimerEnfant(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<Enfant>> getAllEnfants() {
        return ResponseEntity.ok(enfantService.getAllEnfants());
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Enfant>> getEnfantsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(enfantService.getEnfantsByPatient(patientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Enfant> getEnfantById(@PathVariable Long id) {
        return ResponseEntity.ok(enfantService.getEnfantById(id));
    }
}