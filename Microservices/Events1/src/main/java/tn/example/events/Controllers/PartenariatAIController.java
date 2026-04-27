    package tn.example.events.Controllers;

    import lombok.RequiredArgsConstructor;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.http.*;
    import org.springframework.http.client.SimpleClientHttpRequestFactory;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.client.RestTemplate;

    import java.util.List;
    import java.util.Map;

    @RestController
    @RequestMapping("/partenariats")
    @RequiredArgsConstructor
    public class PartenariatAIController {

        @Value("${groq.api.key}")
        private String groqApiKey;

        @PostMapping("/guide-ia")
        public ResponseEntity<Map<String, String>> generateGuide(
                @RequestBody Map<String, String> body) {

            String activite = body.getOrDefault("activite", "");
            String secteur  = body.getOrDefault("secteur", "");
            String objectif = body.getOrDefault("objectif", "");

            String prompt = String.format(
                    "Tu es un expert en partenariats médicaux et scientifiques. " +
                            "Une entreprise souhaite devenir partenaire d'une association de nephrologie pediatrique. " +
                            "Informations sur l'entreprise : " +
                            "Activite : %s. Secteur : %s. Objectif : %s. " +
                            "Genere un mail de collaboration professionnel, personnalise et convaincant " +
                            "de 150 a 200 mots en francais. Le message doit : " +
                            "- Commencer par une accroche adaptee a leur secteur " +
                            "- Expliquer la valeur ajoutee de ce partenariat " +
                            "- Mentionner des synergies concretes avec la nephrologie pediatrique " +
                            "- Se terminer par une phrase d engagement. " +
                            "Retourne UNIQUEMENT le message, sans titre ni explication.",
                    activite, secteur, objectif
            );

            String url = "https://api.groq.com/openai/v1/chat/completions";

            Map<String, Object> requestBody = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "max_tokens", 500,
                    "temperature", 0.7
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(5000);
            factory.setReadTimeout(20000);
            RestTemplate restTemplate = new RestTemplate(factory);

            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                        url, HttpMethod.POST, entity, Map.class
                );

                Map responseBody = response.getBody();
                List choices = (List) responseBody.get("choices");
                Map message = (Map) ((Map) choices.get(0)).get("message");
                String content = (String) message.get("content");

                return ResponseEntity.ok(Map.of("message", content.trim()));

            } catch (Exception e) {
                System.err.println("Groq guide-ia error: " + e.getMessage());
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "Erreur IA : " + e.getMessage()));
            }
        }
    }