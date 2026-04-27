package com.nephroforum.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forum_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ForumProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.PATIENT;

    // Communs
    private String bio;
    private String ville;
    private String avatarUrl;
    private String coverUrl;

    // Médecin
    private String specialite;
    private String hopital;
    private String diplomes;

    // Patient (parent de l'enfant malade)
    private String childName;       // Prénom de l'enfant
    private Integer childAge;       // Âge de l'enfant
    private String childDiagnosis;  // Diagnostic de l'enfant
    private String parentRelation;  // Père / Mère / Tuteur

    // Follow (pour plus tard)
    @Builder.Default
    private int followersCount = 0;
    @Builder.Default
    private int followingCount = 0;

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum Role { PATIENT, DOCTOR, ADMIN }
}