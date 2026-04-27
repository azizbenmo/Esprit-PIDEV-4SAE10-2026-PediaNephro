package tn.example.events.dto;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PubMedPublication {
    private String pmid;
    private String titre;
    private String journal;
    private String annee;
    private String auteurs;
    private String lienPubMed;
    private String doi;
}
