package com.pedianephro.subscription.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "patient_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "age_enfant", nullable = false)
    private Integer ageEnfant;

    @Column(name = "mois_depuis_greffe", nullable = false)
    private Integer moisDepuisGreffe;

    @Column(name = "comorbidites", nullable = false)
    private Integer comorbidites; // 0=aucune, 1=légères, 2=sévères

    @Column(name = "frequence_suivi", nullable = false)
    private Integer frequenceSuivi; // 1=mensuel, 2=bimensuel, 3=hebdomadaire

    @Column(name = "a_eu_episode_rejet", nullable = false)
    private Boolean aEuEpisodeRejet = false;

    @Column(name = "nombre_hospitalisations_an", nullable = false)
    private Integer nombreHospitalisationsAn = 0;

    @Column(name = "prend_immunosuppresseurs", nullable = false)
    private Boolean prendImmunosuppresseurs = true;

    @Column(name = "nombre_medicaments_quotidiens", nullable = false)
    private Integer nombreMedicamentsQuotidiens = 1;

    @Column(name = "presence_complication_active", nullable = false)
    private Boolean presenceComplicationActive = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
