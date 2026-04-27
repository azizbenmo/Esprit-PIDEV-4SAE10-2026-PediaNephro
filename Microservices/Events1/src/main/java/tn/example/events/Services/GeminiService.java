package tn.example.events.Services;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public GeminiService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);
        this.restTemplate = new RestTemplate(factory);
    }

    public String preparationEvenement(String nomEvent, String description, String lieu, String date) {
        String prompt = String.format(
                "Tu es un assistant pour PédiaNéphro. " +
                        "Événement: %s. Description: %s. Lieu: %s. Date: %s. " +
                        "Donne 4 points courts en français avec émojis pour se préparer à cet événement.",
                nomEvent, description, lieu, date
        );

        String url = "https://api.groq.com/openai/v1/chat/completions";

        Map<String, Object> requestBody = Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "max_tokens", 300,
                "temperature", 0.7
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class
            );

            Map body = response.getBody();
            List choices = (List) body.get("choices");
            Map choice = (Map) choices.get(0);
            Map message = (Map) choice.get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            System.err.println("Groq error: " + e.getMessage());
            return "⚠️ Service temporairement indisponible.";
        }
    }
}