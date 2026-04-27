package tn.example.events.dto;



import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SponsorSuggestionsResponse {
    private String nomEvent;
    private String description;
    private List<String> secteursExistants;
    private List<SponsorSuggestion> suggestions;
}
