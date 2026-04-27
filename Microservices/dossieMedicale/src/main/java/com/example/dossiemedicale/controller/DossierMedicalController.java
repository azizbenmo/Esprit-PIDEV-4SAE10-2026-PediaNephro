package com.example.dossiemedicale.controller;

import com.example.dossiemedicale.DTO.AnalyseEvolutionResponse;
import com.example.dossiemedicale.DTO.ComparaisonHospitalisationResponse;
import com.example.dossiemedicale.DTO.DossierResumeResponse;
import com.example.dossiemedicale.DTO.RapportConsultationParentResponse;
import com.example.dossiemedicale.entities.DossierMedical;
import com.example.dossiemedicale.services.ComparaisonHospitalisationService;
import com.example.dossiemedicale.services.DossierEvolutionCliniqueService;
import com.example.dossiemedicale.services.DossierMedicalService;
import com.example.dossiemedicale.services.DossierResumeService;
import com.example.dossiemedicale.services.RapportConsultationExterneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dossiers-medicaux")
@RequiredArgsConstructor
public class DossierMedicalController {

    private final DossierMedicalService dossierMedicalService;

    private final DossierResumeService dossierResumeService;

    private final DossierEvolutionCliniqueService dossierEvolutionCliniqueService;

    private final RapportConsultationExterneService rapportConsultationExterneService;


    private final ComparaisonHospitalisationService comparaisonService;

    @PostMapping("/ajouter")
    public ResponseEntity<DossierMedical> ajouterDossierMedical(@Valid @RequestBody com.example.dossiemedicale.DTO.DossierMedicalRequest request) {
        DossierMedical saved = dossierMedicalService.ajouterDossierMedical(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/modifier/{id}")
    public ResponseEntity<DossierMedical> modifierDossierMedical(
            @PathVariable Long id,
            @Valid @RequestBody com.example.dossiemedicale.DTO.DossierMedicalRequest request
    ) {
        return ResponseEntity.ok(dossierMedicalService.modifierDossierMedical(id, request));
    }

    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<Void> supprimerDossierMedical(@PathVariable Long id) {
        dossierMedicalService.supprimerDossierMedical(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<DossierMedical>> getAllDossiersMedicaux() {
        return ResponseEntity.ok(dossierMedicalService.getAllDossiersMedicaux());
    }

    /**
     * Dossiers dont l’enfant appartient à ce patient (compte parent).
     * Paramètre de requête pour éviter tout conflit avec GET /{id}.
     */
    @GetMapping(params = "patientId")
    public ResponseEntity<List<DossierMedical>> getDossiersByPatient(
            @RequestParam("patientId") Long patientId) {
        return ResponseEntity.ok(dossierMedicalService.getDossiersByPatientId(patientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DossierMedical> getDossierMedicalById(@PathVariable Long id) {
        return ResponseEntity.ok(dossierMedicalService.getDossierMedicalById(id));
    }

    @GetMapping("/{id}/resume-avance")
    public ResponseEntity<DossierResumeResponse> getResumeAvance(@PathVariable("id") Long id) {
        return ResponseEntity.ok(dossierResumeService.getResumeComplet(id));
    }

    @GetMapping("/{id}/analyse-evolution")
    public ResponseEntity<AnalyseEvolutionResponse> analyserEvolution(@PathVariable("id") Long id) {
        return ResponseEntity.ok(dossierEvolutionCliniqueService.analyserEvolution(id));
    }

    @GetMapping("/hospitalisation/{id}/comparaison")
    public ResponseEntity<ComparaisonHospitalisationResponse> comparer(@PathVariable Long id) {
        return ResponseEntity.ok(comparaisonService.comparer(id));
    }

    /**
     * Rapports de consultation du dossier, visibles uniquement par le parent connecte proprietaire du dossier.
     */
    @GetMapping("/{id}/rapports-parent")
    public ResponseEntity<List<RapportConsultationParentResponse>> getRapportsParent(
            @PathVariable("id") Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(
                rapportConsultationExterneService.getRapportsPourParentConnecte(id, authorizationHeader)
        );
    }

}
