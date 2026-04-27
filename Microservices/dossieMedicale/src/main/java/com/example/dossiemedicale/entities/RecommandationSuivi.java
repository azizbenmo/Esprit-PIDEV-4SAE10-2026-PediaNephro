package com.example.dossiemedicale.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommandation_suivi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecommandationSuivi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recommandation")
    private Long id;

    @Column(name = "dossier_id", nullable = false)
    private Long dossierId;

    @Column(name = "specialite")
    private String specialite;

    @Column(name = "examens_recommandes", columnDefinition = "TEXT")
    private String examensRecommandes;

    @Column(name = "rappel_controle")
    private String rappelControle;

    @Column(name = "conseils", columnDefinition = "TEXT")
    private String conseils;

    @Column(name = "niveau_priorite")
    private String niveauPriorite;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "source")
    private String source;
}