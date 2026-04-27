package com.example.dossiemedicale.DTO;

import lombok.Data;

/**
 * Crée ou retrouve le patient « dossier médical » associé au compte connecté (clé : email).
 */
@Data
public class AssurerComptePatientRequest {
    private String email;
    private String prenom;
    private String nom;
    /** id du compte connecté côté microservice User (parent). */
    private Long userId;
}
