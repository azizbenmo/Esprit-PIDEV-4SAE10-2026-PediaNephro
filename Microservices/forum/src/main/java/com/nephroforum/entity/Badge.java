package com.nephroforum.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "badges")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ownerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeType type;

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime earnedAt = LocalDateTime.now();

    public enum BadgeType {
        EXPERT_REIN,       // Docteur très actif
        GUERRIER_COURAGEUX, // Patient qui poste régulièrement
        MEILLEURE_REPONSE   // Réponse marquée comme solution
    }
}