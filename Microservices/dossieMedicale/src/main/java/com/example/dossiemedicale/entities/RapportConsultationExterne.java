package com.example.dossiemedicale.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rapport_consultation_externe")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RapportConsultationExterne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consultation_id", nullable = false, unique = true)
    private Long consultationId;

    @Column(name = "patient_user_id")
    private Long patientUserId;

    @Column(name = "medecin_user_id")
    private Long medecinUserId;

    @Column(name = "medecin_prenom")
    private String medecinPrenom;

    @Column(name = "medecin_nom")
    private String medecinNom;

    @Column(name = "enfant_prenom")
    private String enfantPrenom;

    @Column(name = "enfant_nom")
    private String enfantNom;

    @Column(name = "parent_prenom")
    private String parentPrenom;

    @Column(name = "parent_nom")
    private String parentNom;

    private String specialite;
    private String motif;

    @Column(columnDefinition = "TEXT")
    private String diagnostic;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(columnDefinition = "TEXT")
    private String recommandations;

    private String etatPatient;
    private String fichierPath;

    private LocalDateTime dateSouhaitee;
    private LocalDateTime dateConfirmee;
    private LocalDateTime dateRapport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id")
    @JsonIgnore
    private DossierMedical dossier;
}
