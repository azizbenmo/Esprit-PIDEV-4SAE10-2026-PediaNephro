package com.example.dossiemedicale.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "alerte")
@ToString
public class Alerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerte")
    private Long idAlerte;

    @Column(name = "niveau")
    private String niveau;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_declenchement")
    private Date dateDeclenchement;

    @ManyToOne
    @JoinColumn(name = "constante_id")
    private ConstanteVitale constante;
}
