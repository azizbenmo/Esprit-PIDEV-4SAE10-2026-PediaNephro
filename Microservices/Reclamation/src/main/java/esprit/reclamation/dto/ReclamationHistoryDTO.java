package esprit.reclamation.dto;

import esprit.reclamation.entities.StatutReclamation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReclamationHistoryDTO {

    private Long id;
    private Long reclamationId;
    private StatutReclamation ancienStatut;
    private StatutReclamation nouveauStatut;
    private Long acteurId;
    private String commentaire;
    private LocalDateTime dateChangement;
}
