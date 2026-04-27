package com.example.dossiemedicale.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "localisation_client")
@ToString
public class LocalisationClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_localisation")
    private Long idLocalisation;

    @ManyToOne
    @JoinColumn(name = "enfant_id", nullable = false)
    @JsonIgnoreProperties({"hospitalisations", "dossierMedical", "patient"})
    private Enfant enfant;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "precision_m")
    private Double precisionM;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "horodatage", nullable = false)
    private Date horodatage;

    @Column(name = "source_localisation")
    private String sourceLocalisation;
}