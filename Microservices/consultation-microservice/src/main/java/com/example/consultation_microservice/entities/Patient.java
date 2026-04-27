package com.example.consultation_microservice.entities;

import jakarta.persistence.Column;
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
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifiant utilisateur / patient côté microservice User (même id que {@code users.id} pour un enfant).
     * Permet de lier les demandes même si la clé primaire locale diffère.
     */
    @Column(name = "user_id")
    private Long userId;

    private String nom;

    private String prenom;

    private String email;
}
