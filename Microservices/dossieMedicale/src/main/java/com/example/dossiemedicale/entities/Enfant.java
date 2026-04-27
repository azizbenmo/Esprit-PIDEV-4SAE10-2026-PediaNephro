package com.example.dossiemedicale.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "enfant")
public class Enfant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_enfant")
    private Long idEnfant;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "age")
    private Integer age;

    @Column(name = "sexe")
    private String sexe;

    @Column(name = "taille")
    private Double taille;

    @Column(name = "poids")
    private Double poids;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @OneToOne(mappedBy = "enfant")
    @JsonIgnore
    private DossierMedical dossierMedical;
    @OneToMany(mappedBy = "enfant", cascade = CascadeType.ALL)
    @JsonIgnore
    private java.util.List<Hospitalisation> hospitalisations;

    // Dans Enfant.java
    @Column(name = "user_patient_id")
    private Long userPatientId;   // id du Patient dans le microservice User

}