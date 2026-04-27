package com.example.dossiemedicale.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SyncRapportConsultationRequest {
    private Long consultationId;
    private Long dossierMedicalId;
    private Long patientUserId;
    private Long medecinUserId;

    private String medecinPrenom;
    private String medecinNom;

    private String enfantPrenom;
    private String enfantNom;
    private String parentPrenom;
    private String parentNom;

    private String specialite;
    private String motif;

    private String diagnostic;
    private String observations;
    private String recommandations;
    private String etatPatient;
    private String fichierPath;

    private LocalDateTime dateSouhaitee;
    private LocalDateTime dateConfirmee;
    private LocalDateTime dateRapport;
}
