package tn.example.events.dto;



import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class InscriptionEventDTO {
    // Inscription
    private Long idInscription;
    private String statut;
    private String typeParticipant;
    private LocalDateTime dateInscription;

    // Event
    private Long idEvent;
    private String nomEvent;
    private String description;
    private String lieu;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private int capacite;
    private String imageBase64;
}
