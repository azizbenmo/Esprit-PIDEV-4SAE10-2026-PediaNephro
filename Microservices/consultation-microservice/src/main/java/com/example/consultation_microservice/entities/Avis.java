package com.example.consultation_microservice.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Medecin medecin;

    @ManyToOne
    private Patient patient;

    @ManyToOne
    private Consultation consultation;

    private Integer note;

    private String commentaire;

    private LocalDateTime dateAvis;
}