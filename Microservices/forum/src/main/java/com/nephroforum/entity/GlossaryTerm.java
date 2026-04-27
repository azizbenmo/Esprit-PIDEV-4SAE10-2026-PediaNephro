package com.nephroforum.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "glossary_terms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GlossaryTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String term;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String definition;

    @Column(columnDefinition = "TEXT")
    private String simpleDefinition; // Version simplifiée pour les patients

    private String category; // ex: "Symptôme", "Traitement", "Analyse"
}