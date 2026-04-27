package com.example.dossiemedicale.controller;

import com.example.dossiemedicale.DTO.AssurerComptePatientRequest;
import com.example.dossiemedicale.entities.Patient;
import com.example.dossiemedicale.services.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    /**
     * Pour un parent connecté : retourne l'id patient dossier existant ou en crée un lié à l'email du compte.
     */
    @PostMapping("/assurer-compte")
    public ResponseEntity<?> assurerCompte(@RequestBody AssurerComptePatientRequest body) {
        if (body == null || body.getEmail() == null || body.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("L'email est obligatoire.");
        }
        try {
            Patient p = patientService.assurerPatientPourCompte(
                    body.getEmail(),
                    body.getPrenom(),
                    body.getNom(),
                    body.getUserId());
            return ResponseEntity.ok(p);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/ajouter")
    public ResponseEntity<Patient> ajouterPatient(@RequestBody Patient patient) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(patientService.ajouterPatient(patient));
    }

    @PutMapping("/modifier/{id}")
    public ResponseEntity<Patient> modifierPatient(
            @PathVariable Long id,
            @RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.modifierPatient(id, patient));
    }

    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<Void> supprimerPatient(@PathVariable Long id) {
        patientService.supprimerPatient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }
}
