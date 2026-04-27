package com.example.consultation_microservice.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class OllamaService {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String genererResume(String prompt) {
        try {
            Map<String, Object> body = Map.of(
                    "model", "mistral",
                    "prompt", prompt,
                    "stream", false
            );

            String requestBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            Map<?, ?> responseMap = objectMapper.readValue(response.body(), Map.class);
            return (String) responseMap.get("response");

        } catch (Exception e) {
            return "Erreur lors de la génération du résumé : " + e.getMessage();
        }
    }
}