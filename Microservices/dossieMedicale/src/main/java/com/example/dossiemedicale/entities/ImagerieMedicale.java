package com.example.dossiemedicale.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImagerieMedicale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImagerie;

    private String type;

    @Column(length = 5000)
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateExamen;

    private String cheminFichier;

    // 🔥 RELATION OBLIGATOIRE
    @ManyToOne
    @JoinColumn(name = "dossier_id", nullable = false)
    private DossierMedical dossier;
}
