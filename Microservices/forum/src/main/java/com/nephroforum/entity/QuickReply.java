package com.nephroforum.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quick_replies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuickReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String label; // Texte affiché sur le bouton

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // Contenu inséré dans le commentaire

    @Column(nullable = false)
    private String ownerName; // Médecin qui a créé cette réponse rapide
}