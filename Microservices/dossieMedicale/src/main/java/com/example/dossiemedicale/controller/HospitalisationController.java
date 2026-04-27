package com.example.dossiemedicale.controller;

import com.example.dossiemedicale.DTO.HospitalisationRequest;
import com.example.dossiemedicale.entities.Hospitalisation;
import com.example.dossiemedicale.services.HospitalisationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hospitalisations")
@RequiredArgsConstructor
public class HospitalisationController {

    private final HospitalisationService hospitalisationService;

    @PostMapping("/ajouter")
    public ResponseEntity<Hospitalisation> ajouter(@RequestBody HospitalisationRequest request) {
        Hospitalisation saved = hospitalisationService.ajouterHospitalisation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/modifier/{id}")
    public ResponseEntity<Hospitalisation> modifier(@PathVariable Long id,
                                                    @RequestBody HospitalisationRequest request) {
        return ResponseEntity.ok(hospitalisationService.modifierHospitalisation(id, request));
    }

    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        hospitalisationService.supprimerHospitalisation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hospitalisation> getById(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalisationService.getHospitalisationById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Hospitalisation>> all() {
        return ResponseEntity.ok(hospitalisationService.getAllHospitalisations());
    }
}