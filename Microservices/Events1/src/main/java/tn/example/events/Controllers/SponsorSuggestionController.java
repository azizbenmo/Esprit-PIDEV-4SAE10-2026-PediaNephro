package tn.example.events.Controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.example.events.Entities.Event;
import tn.example.events.Services.EventService;
import tn.example.events.Services.SponsorSuggestionService;
import tn.example.events.dto.SponsorSuggestionsResponse;

import java.util.Map;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class SponsorSuggestionController {

    private final SponsorSuggestionService sponsorSuggestionService;
    private final EventService eventService;

    @PostMapping("/{id}/suggestions-sponsors")
    public ResponseEntity<SponsorSuggestionsResponse> getSuggestions(
            @PathVariable Long id) {
        Event event = eventService.getById(id);
        SponsorSuggestionsResponse response =
                sponsorSuggestionService.genererSuggestions(event);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/suggestions-sponsors/email")
    public ResponseEntity<Map<String, String>> genererEmail(
            @RequestBody Map<String, String> body) {
        String result = sponsorSuggestionService.genererEmail(
                body.get("nomEvent"),
                body.get("nomSuggere"),
                body.get("secteur"),
                body.get("raisonSuggestion")
        );
        return ResponseEntity.ok(Map.of("email", result));
    }
}
