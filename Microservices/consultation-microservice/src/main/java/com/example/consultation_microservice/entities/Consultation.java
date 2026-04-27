package com.example.consultation_microservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Consultation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Patient patient;

    @ManyToOne
    private Medecin medecin;

    private LocalDateTime dateSouhaitee;
    private LocalDateTime dateConfirmee;

    @Enumerated(EnumType.STRING)
    private ConsultationStatus statut;

    private String specialite;
    private String motif;


    private String symptomes;        // ex: "oedèmes, hypertension, fatigue"
    private String antecedents;      // ex: "syndrome néphrotique, diabète"
    private String resultatsBio;     // ex: "créatinine élevée, protéinurie"

    @Enumerated(EnumType.STRING)
    private NiveauUrgence niveauUrgence;  // URGENTE, PRIORITAIRE, NORMALE

    private Integer scoreUrgence;    // 0-100
    private String justificationTriage;

    /**
     * Parent demandeur (compte PARENT) — distinct du patient enfant (lié via {@link #patient}).
     */
    @Column(name = "demandeur_parent_prenom", length = 120)
    private String demandeurParentPrenom;

    @Column(name = "demandeur_parent_nom", length = 120)
    private String demandeurParentNom;

    /**
     * Email du parent demandeur (compte PARENT). Utilise pour notifications parent
     * à la création et à la décision de consultation.
     */
    @Column(name = "demandeur_parent_email", length = 255)
    private String demandeurParentEmail;

}
