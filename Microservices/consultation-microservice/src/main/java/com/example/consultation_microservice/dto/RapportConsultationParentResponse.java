package com.example.consultation_microservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class RapportConsultationParentResponse {
    private Long id;
    private Long consultationId;

    private String diagnostic;
    private String observations;
    private String recommandations;
    private String etatPatient;
    private LocalDateTime dateRapport;
    private String fichierPath;

    private String enfantPrenom;
    private String enfantNom;
    private String parentPrenom;
    private String parentNom;
    private String medecinPrenom;
    private String medecinNom;

    private String specialite;
    private String motif;
}
