package com.example.dossiemedicale.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "examen")
@ToString(exclude = {"fichierExamen"})
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_examen")
    private Long idExamen;

    @Column(name = "type")
    private String type;

    @Column(name = "resultat", columnDefinition = "TEXT")
    private String resultat;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_examen")
    private Date dateExamen;

    @ManyToOne
    @JoinColumn(name = "dossier_id", nullable = false)
    private DossierMedical dossier;

    @Column(name = "nom_fichier")
    private String nomFichier;

    @Column(name = "type_fichier")
    private String typeFichier;

    @Lob
    @Column(name = "fichier_examen", columnDefinition = "LONGBLOB")
    private byte[] fichierExamen;
}