package com.example.consultation_microservice.controllers;

import com.example.consultation_microservice.dto.RapportConsultationParentResponse;
import com.example.consultation_microservice.entities.Consultation;
import com.example.consultation_microservice.entities.Medecin;
import com.example.consultation_microservice.entities.Patient;
import com.example.consultation_microservice.entities.RapportConsultation;
import com.example.consultation_microservice.repositories.RapportConsultationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/apiConsultation/internal/rapports-consultation")
@RequiredArgsConstructor
public class InternalRapportConsultationController {

    private final RapportConsultationRepository rapportConsultationRepository;

    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<RapportConsultationParentResponse>> getSubmittedReportsByDossier(
            @PathVariable("dossierId") Long dossierId) {
        List<RapportConsultationParentResponse> rapports = rapportConsultationRepository
                .findSubmittedByDossierMedicalId(dossierId)
                .stream()
                .map(this::toParentResponse)
                .toList();

        return ResponseEntity.ok(rapports);
    }

    @GetMapping("/patient-user/{patientUserId}")
    public ResponseEntity<List<RapportConsultationParentResponse>> getSubmittedReportsByPatientUser(
            @PathVariable("patientUserId") Long patientUserId) {
        List<RapportConsultationParentResponse> rapports = rapportConsultationRepository
                .findSubmittedByPatientUserId(patientUserId)
                .stream()
                .map(this::toParentResponse)
                .toList();

        return ResponseEntity.ok(rapports);
    }

    private RapportConsultationParentResponse toParentResponse(RapportConsultation rapport) {
        Consultation consultation = rapport.getConsultation();
        Patient patient = consultation != null ? consultation.getPatient() : null;
        Medecin medecin = consultation != null ? consultation.getMedecin() : null;

        return RapportConsultationParentResponse.builder()
                .id(rapport.getId())
                .consultationId(consultation != null ? consultation.getId() : null)
                .diagnostic(rapport.getDiagnostic())
                .observations(rapport.getObservations())
                .recommandations(rapport.getRecommandations())
                .etatPatient(rapport.getEtatPatient())
                .dateRapport(rapport.getCreatedAt())
                .fichierPath(rapport.getFichierPath())
                .enfantPrenom(patient != null ? patient.getPrenom() : null)
                .enfantNom(patient != null ? patient.getNom() : null)
                .parentPrenom(consultation != null ? consultation.getDemandeurParentPrenom() : null)
                .parentNom(consultation != null ? consultation.getDemandeurParentNom() : null)
                .medecinPrenom(medecin != null ? medecin.getPrenom() : null)
                .medecinNom(medecin != null ? medecin.getNom() : null)
                .specialite(consultation != null ? consultation.getSpecialite() : null)
                .motif(consultation != null ? consultation.getMotif() : null)
                .build();
    }
}
