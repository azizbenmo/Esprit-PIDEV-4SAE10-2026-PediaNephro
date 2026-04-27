package tn.example.events.Controllers;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.example.events.Services.GeminiService;
import java.util.Map;

@RestController
@RequestMapping("/gemini")
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;

    @PostConstruct
    public void init() {
        System.out.println("✅ GeminiController LOADED");
    }

    @PostMapping("/preparation")
    public ResponseEntity<Map<String, String>> getPreparation(
            @RequestBody Map<String, String> body) {

        String result = geminiService.preparationEvenement(
                body.get("nomEvent"),
                body.get("description"),
                body.get("lieu"),
                body.get("date")
        );

        return ResponseEntity.ok(Map.of("preparation", result));
    }
}