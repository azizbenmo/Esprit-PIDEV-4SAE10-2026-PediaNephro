package com.example.dossiemedicale.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "constante_prediction")
public class ConstantePrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prediction")
    private Long idPrediction;

    private String type;

    @Column(name = "valeur_predite")
    private Double valeurPredite;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_prediction")
    private Date datePrediction;

    @ManyToOne
    @JoinColumn(name = "dossier_id")
    private DossierMedical dossier;

    @ManyToOne
    @JoinColumn(name = "constante_source_id")
    private ConstanteVitale constanteSource;
}