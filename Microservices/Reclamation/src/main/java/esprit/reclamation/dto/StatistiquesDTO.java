package esprit.reclamation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesDTO {

    private long total;
    private long enAttente;
    private long enCours;
    private long resolues;
    private long rejetees;
}

