package com.example.consultation_microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload envoyé par le microservice User pour aligner {@code medecin.user_id} sur {@code users.id}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedecinSyncRequest {

    private Long userId;
    private String email;
    /** Nom complet (ex. inscription) ; sera découpé en prénom / nom. */
    private String fullName;
    private String specialite;
    private String telephone;
    private Integer anneesExperience;
    /** {@code false} = ne plus proposer en ligne (refus admin). */
    private Boolean disponible;
    private String ville;
}
