package com.example.consultation_microservice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Medecin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Lien vers le compte User : identique à {@code users.id} (et à {@code doctors.id} avec {@code @MapsId}).
     * Sert d’ancrage pour l’annuaire et la résolution médecin à l’enregistrement d’une consultation.
     */
    private Long userId;

    private String nom;

    private String prenom;

    private String email;

    private String specialite;
    private Boolean disponible;

    private String adresseCabinet;

    private String ville;

    private String telephone;

    private String bio;

    private String languesParlees;

    private Integer anneesExperience;

    private String photoUrl;

    private String googleMapsUrl;



}
