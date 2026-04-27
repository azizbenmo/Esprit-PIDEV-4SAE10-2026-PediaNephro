package tn.example.events.Services;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.example.events.Entities.Event;
import tn.example.events.Entities.Partenariat;
import tn.example.events.Entities.StatutPartenariat;
import tn.example.events.dto.SponsorSuggestion;
import tn.example.events.dto.SponsorSuggestionsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SponsorSuggestionService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final ObjectMapper objectMapper;
    private final PartenariatService partenariatService;

    public SponsorSuggestionsResponse genererSuggestions(Event event) {
        // Étape 1 — Récupérer partenariats existants acceptés
        List<Partenariat> tousPartenariats = partenariatService.getAll();

        List<String> secteursExistants = tousPartenariats.stream()
                .filter(p -> p.getStatut() == StatutPartenariat.ACCEPTE)
                .map(Partenariat::getNomEntreprise)
                .collect(Collectors.toList());

        List<String> nomsExistants = tousPartenariats.stream()
                .map(Partenariat::getNomEntreprise)
                .collect(Collectors.toList());

        // Étape 2 — Construire prompt
        String prompt = buildPrompt(event, secteursExistants, nomsExistants);

        // Étape 3 — Appel Groq
        String url = "https://api.groq.com/openai/v1/chat/completions";

        Map<String, Object> requestBody = Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", List.of(
                        Map.of("role", "system", "content", getSystemPrompt()),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7,
                "max_tokens", 2000
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        RestTemplate restTemplate = new RestTemplate(factory);

        try {
            log.info("📡 Génération suggestions sponsors pour : {}", event.getNomEvent());

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            Map responseBody = response.getBody();
            List choices = (List) responseBody.get("choices");
            Map message = (Map) ((Map) choices.get(0)).get("message");
            String content = ((String) message.get("content"))
                    .replaceAll("```json", "").replaceAll("```", "").trim();

            log.info("✅ Suggestions reçues de Groq");

            List<SponsorSuggestion> suggestions = parseSuggestions(content);

            return SponsorSuggestionsResponse.builder()
                    .nomEvent(event.getNomEvent())
                    .description(event.getDescription())
                    .secteursExistants(secteursExistants)
                    .suggestions(suggestions)
                    .build();

        } catch (Exception e) {
            log.error("❌ Erreur génération suggestions: {}", e.getMessage());
            return SponsorSuggestionsResponse.builder()
                    .nomEvent(event.getNomEvent())
                    .secteursExistants(secteursExistants)
                    .suggestions(List.of())
                    .build();
        }
    }

    public String genererEmail(String nomEvent, String nomSuggere,
                               String secteur, String raisonSuggestion) {
        String url = "https://api.groq.com/openai/v1/chat/completions";

        String prompt = String.format("""
            Rédige un email de démarchage professionnel en français pour contacter %s,
            une entreprise du secteur %s, afin qu'elle devienne sponsor de l'événement "%s".
            
            Raison de la suggestion : %s
            
            L'email doit :
            - Avoir un objet accrocheur
            - Se présenter comme PediaNephro (plateforme médicale pédiatrie-néphrologie)
            - Expliquer la valeur du partenariat
            - Être entre 150 et 200 mots
            - Terminer par un appel à l'action clair
            
            Réponds en JSON : { "objet": "...", "corps": "..." }
            """, nomSuggere, secteur, nomEvent, raisonSuggestion);

        Map<String, Object> requestBody = Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.7,
                "max_tokens", 800
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        RestTemplate restTemplate = new RestTemplate(factory);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            Map responseBody = response.getBody();
            List choices = (List) responseBody.get("choices");
            Map message = (Map) ((Map) choices.get(0)).get("message");
            String content = ((String) message.get("content"))
                    .replaceAll("```json", "").replaceAll("```", "").trim();

            return content;

        } catch (Exception e) {
            log.error("❌ Erreur génération email: {}", e.getMessage());
            return "{\"objet\": \"Erreur\", \"corps\": \"Erreur génération email.\"}";
        }
    }

    private String getSystemPrompt() {
        return """
            Tu es un expert en développement de partenariats pour PediaNephro,
            une plateforme médicale spécialisée en pédiatrie-néphrologie.
            
            Tu analyses les événements médicaux et suggères des sponsors pertinents.
            
            Réponds UNIQUEMENT en JSON valide, sans markdown :
            {
              "suggestions": [
                {
                  "nomSuggere": "Nom type d'entreprise suggérée",
                  "secteur": "Secteur d'activité",
                  "scoreMatch": 85,
                  "raisonSuggestion": "Explication courte en français",
                  "typeContact": "Email | LinkedIn | Téléphone"
                }
              ]
            }
            
            Règles :
            - Suggère exactement 5 sponsors
            - scoreMatch entre 60 et 100
            - Évite les secteurs déjà partenaires
            - Reste dans le domaine médical/santé/pharma/tech santé
            - Sois spécifique et réaliste
            """;
    }

    private String buildPrompt(Event event, List<String> secteursExistants,
                               List<String> nomsExistants) {
        return String.format("""
            Événement à sponsoriser :
            - Nom : %s
            - Description : %s
            - Lieu : %s
            - Date : %s
            - Capacité : %d participants
            
            Partenaires déjà existants (à ne pas re-suggérer) :
            %s
            
            Suggère 5 nouveaux types de sponsors externes pertinents pour cet événement.
            """,
                event.getNomEvent(),
                event.getDescription() != null ? event.getDescription() : "Non renseigné",
                event.getLieu() != null ? event.getLieu() : "Non renseigné",
                event.getDateDebut() != null ? event.getDateDebut().toString() : "Non renseigné",
                event.getCapacite(),
                nomsExistants.isEmpty() ? "Aucun partenaire existant" : String.join(", ", nomsExistants)
        );
    }

    private List<SponsorSuggestion> parseSuggestions(String content) throws Exception {
        JsonNode root = objectMapper.readTree(content);
        JsonNode suggestionsNode = root.path("suggestions");

        List<SponsorSuggestion> suggestions = new ArrayList<>();
        if (suggestionsNode.isArray()) {
            for (JsonNode node : suggestionsNode) {
                suggestions.add(SponsorSuggestion.builder()
                        .nomSuggere(node.path("nomSuggere").asText())
                        .secteur(node.path("secteur").asText())
                        .scoreMatch(node.path("scoreMatch").asInt())
                        .raisonSuggestion(node.path("raisonSuggestion").asText())
                        .typeContact(node.path("typeContact").asText())
                        .build());
            }
        }
        return suggestions;
    }
}
