package com.example.consultation_microservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class DossierMedical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ One dossier per patient (patientId links to Patient microservice or table)
    @Column(unique = true, nullable = false)
    private Long patientId;

    // ✅ All consultation reports attached to this dossier
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_medical_id")
    private List<RapportConsultation> rapports = new ArrayList<>();
}