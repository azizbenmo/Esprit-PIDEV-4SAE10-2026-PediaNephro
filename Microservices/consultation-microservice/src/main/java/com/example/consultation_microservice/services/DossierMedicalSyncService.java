package com.example.consultation_microservice.services;

import com.example.consultation_microservice.dto.SyncRapportConsultationRequest;
import com.example.consultation_microservice.entities.Consultation;
import com.example.consultation_microservice.entities.RapportConsultation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class DossierMedicalSyncService {

    private final RestTemplate restTemplate;

    @Value("${microservice.dossier.url:http://localhost:8012/dossiemedicale}")
    private String dossierMedicalBaseUrl;

    public void syncRapportConsultation(Consultation consultation, RapportConsultation rapport, Long dossierMedicalId) {
        if (consultation == null || consultation.getPatient() == null || rapport == null) {
            throw new IllegalArgumentException("Donnees de synchronisation invalides");
        }

        SyncRapportConsultationRequest req = new SyncRapportConsultationRequest();
        req.setConsultationId(consultation.getId());
        req.setDossierMedicalId(dossierMedicalId);
        req.setPatientUserId(consultation.getPatient().getUserId() != null
                ? consultation.getPatient().getUserId()
                : consultation.getPatient().getId());
        req.setMedecinUserId(consultation.getMedecin() != null
                ? (consultation.getMedecin().getUserId() != null
                ? consultation.getMedecin().getUserId()
                : consultation.getMedecin().getId())
                : null);
        req.setMedecinPrenom(consultation.getMedecin() != null ? consultation.getMedecin().getPrenom() : null);
        req.setMedecinNom(consultation.getMedecin() != null ? consultation.getMedecin().getNom() : null);
        req.setEnfantPrenom(consultation.getPatient().getPrenom());
        req.setEnfantNom(consultation.getPatient().getNom());
        req.setParentPrenom(consultation.getDemandeurParentPrenom());
        req.setParentNom(consultation.getDemandeurParentNom());
        req.setSpecialite(consultation.getSpecialite());
        req.setMotif(consultation.getMotif());
        req.setDiagnostic(rapport.getDiagnostic());
        req.setObservations(rapport.getObservations());
        req.setRecommandations(rapport.getRecommandations());
        req.setEtatPatient(rapport.getEtatPatient());
        req.setFichierPath(rapport.getFichierPath());
        req.setDateSouhaitee(consultation.getDateSouhaitee());
        req.setDateConfirmee(consultation.getDateConfirmee());
        req.setDateRapport(rapport.getCreatedAt());

        restTemplate.postForEntity(
                dossierMedicalBaseUrl + "/internal/rapports-consultation/sync",
                req,
                Void.class
        );
    }
}
