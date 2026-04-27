package tn.example.events.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SponsorSuggestion {
    private String nomSuggere;
    private String secteur;
    private int scoreMatch;
    private String raisonSuggestion;
    private String typeContact;
    private String emailTemplate;
}
