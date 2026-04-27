package com.example.dossiemedicale.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dossier_medical")
public class DossierMedical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dossier")
    private Long idDossier;

    @Column(name = "code", unique = true, nullable = false, length = 32)
    private String code;

    @Column(name = "date_creation")
    private LocalDate dateCreation;

    @OneToOne
    @JoinColumn(name = "enfant_id", unique = true, nullable = false)
    private Enfant enfant;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ConstanteVitale> constantesVitales;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Examen> examens;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ImagerieMedicale> imageriesMedicales;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<RapportConsultationExterne> rapportsConsultation = new ArrayList<>();
}
