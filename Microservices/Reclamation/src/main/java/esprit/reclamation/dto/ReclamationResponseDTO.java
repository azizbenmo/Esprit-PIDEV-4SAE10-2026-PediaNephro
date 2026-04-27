package esprit.reclamation.dto;

import esprit.reclamation.entities.CategorieReclamation;
import esprit.reclamation.entities.Priorite;
import esprit.reclamation.entities.PrioriteReclamation;
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
public class ReclamationResponseDTO {

    private Long id;
    private String titre;
    private String description;
    private StatutReclamation statut;
    private Priorite priorite;
    private CategorieReclamation categorie;
    private PrioriteReclamation prioriteReclamation;
    private LocalDateTime slaDeadline;
    private Boolean escaladee;
    private Long userId;
    private String userUsername;
    private String userEmail;
    private Long adminId;
    private String reponse;
    private LocalDateTime dateCreation;
    private LocalDateTime dateTraitement;
}
