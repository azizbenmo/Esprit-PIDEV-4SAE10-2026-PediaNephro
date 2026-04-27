package com.example.consultation_microservice.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class RapportConsultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "consultation_id", unique = true) // ✅ 1 rapport par consultation
    private Consultation consultation;

    private String diagnostic;
    private String observations;
    private String recommandations;
    private String etatPatient;

    private LocalDateTime createdAt = LocalDateTime.now();
    private String fichierPath;

    // ✅ false = brouillon (créé à l'ouverture), true = soumis par le médecin
    private boolean estSoumis = false;

    @Column(name = "dossier_medical_id", insertable = false, updatable = false)
    private Long dossierMedicalId;
}