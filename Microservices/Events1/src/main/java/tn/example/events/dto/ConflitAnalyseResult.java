package tn.example.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflitAnalyseResult {
    private boolean conflitDetecte;
    private String niveauRisque; // "FAIBLE", "MOYEN", "ELEVE", "CRITIQUE"
    private String resume;
    private List<String> conflitsConcurrents;
    private List<String> controversesMedicales;
    private List<String> conflitsEthiques;
    private String recommandation; // "APPROUVER", "EXAMINER", "REFUSER"
    private String justification;
}
