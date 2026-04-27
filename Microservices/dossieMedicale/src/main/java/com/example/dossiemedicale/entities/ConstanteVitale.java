package com.example.dossiemedicale.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "constante_vitale")
@ToString
public class ConstanteVitale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_constante")
    private Long idConstante;

    @Column(name = "type")
    private String type;
    
    @Column(name = "valeur")
    private Double valeur;
    
    @Column(name = "seuil_min")
    private Double seuilMin;
    
    @Column(name = "seuil_max")
    private Double seuilMax;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_mesure")
    private Date dateMesure;

    @ManyToOne
    @JoinColumn(name = "dossier_id")
    private DossierMedical dossier;

    @OneToMany(mappedBy = "constante", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Alerte> alertes;
}
