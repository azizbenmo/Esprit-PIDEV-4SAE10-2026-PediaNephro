package tn.example.events.Entities;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInscription;

    private LocalDateTime dateInscription;

    @Enumerated(EnumType.STRING)
    private Statut statut; // "confirmé", "en attente"

    @Enumerated(EnumType.STRING)
    private TypeParticipant typeParticipant;

    // Relation ManyToOne vers Event
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    // Relation ManyToOne vers Participant
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "participant_id")
    @JsonIgnoreProperties({"inscriptions", "hibernateLazyInitializer", "handler"})
    private Participant participant;
}