package tn.example.events.dto;



import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class MonEspaceDTO {
    private Long idParticipant;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private MonEspaceStatsDTO stats;
    private List<InscriptionEventDTO> aVenir;
    private List<InscriptionEventDTO> historique;
    private List<InscriptionEventDTO> enAttente;
}
