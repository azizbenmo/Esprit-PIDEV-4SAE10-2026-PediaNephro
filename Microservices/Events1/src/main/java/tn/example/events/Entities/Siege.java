package tn.example.events.Entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Siege {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSiege;

    private String numero;
    private String rangee;
    private int position;

    @Enumerated(EnumType.STRING)
    private StatutSiege statut = StatutSiege.LIBRE;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @OneToOne
    @JoinColumn(name = "inscription_id")
    private Inscription inscription;
}
