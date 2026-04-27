package tn.example.events.dto;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonEspaceStatsDTO {
    private long totalInscriptions;
    private long confirmes;
    private long enAttente;
    private long annules;
    private long listeAttente;
    private long passees;
    private long aVenir;
}
