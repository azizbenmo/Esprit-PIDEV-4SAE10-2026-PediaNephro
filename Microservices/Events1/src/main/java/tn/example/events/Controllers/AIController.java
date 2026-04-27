package tn.example.events.Controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.example.events.Services.AIDescriptionService;

import java.util.Map;

@RestController
@RequestMapping("/ai")
/*@CrossOrigin(origins = "http://localhost:4200",
        allowedHeaders = "*",
        methods = {RequestMethod.POST, RequestMethod.OPTIONS})*/
public class AIController {

    private final AIDescriptionService aiDescriptionService;

    public AIController(AIDescriptionService aiDescriptionService) {
        this.aiDescriptionService = aiDescriptionService;
    }

    @PostMapping("/generate-description")
    public ResponseEntity<Map<String, String>> generateDescription(
            @RequestBody Map<String, String> request) {

        String eventName = request.get("eventName");

        if (eventName == null || eventName.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le nom de l'événement est requis"));
        }

        String description = aiDescriptionService.generateDescription(eventName);

        return ResponseEntity.ok(Map.of("description", description));
    }
}
