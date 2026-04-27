package com.example.dossiemedicale.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hospitalisation")
@ToString
public class Hospitalisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_hospitalisation")
    private Long idHospitalisation;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_entree")
    private Date dateEntree;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_sortie")
    private Date dateSortie;

    @Column(name = "motif")
    private String motif;

    @Column(name = "service_hospitalier")
    private String serviceHospitalier;

    @ManyToOne
    @JoinColumn(name = "enfant_id", nullable = false)
    private Enfant enfant;
}