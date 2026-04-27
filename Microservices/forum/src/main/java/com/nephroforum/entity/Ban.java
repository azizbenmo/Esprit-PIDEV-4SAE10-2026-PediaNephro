package com.nephroforum.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ban {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private int violationCount;

    @Column
    private LocalDateTime bannedUntil;

    @Column(nullable = false)
    private boolean active = false;

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;
}