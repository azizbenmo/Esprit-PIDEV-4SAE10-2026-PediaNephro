package com.nephroforum.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GlossaryService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Map<String, String>> detectWithAI(String text) {
        try {
            String prompt = String.format("""
                You are a medical terminology expert.
                
                Analyze this medical text and identify ALL medical terms, symptoms, diseases, treatments, or procedures.
                
                For each term found, provide:
                - term: the exact medical word found in the text
                - definition: a professional medical definition (1-2 sentences)
                - simpleDefinition: a simple explanation for patients (1-2 sentences)
                - category: one of [Symptôme, Maladie, Traitement, Analyse, Procédure, Médicament]
                
                Text to analyze:
                "%s"
                
                Respond ONLY with a valid JSON array. No explanation, no markdown, no code blocks.
                Example format:
                [{"term":"dialyse","definition":"...","simpleDefinition":"...","category":"Traitement"}]
                
                If no medical terms found, return empty array: []
                """, text);

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", "phi3",
                    "prompt", prompt,
                    "stream", false
            ));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            JsonNode node = objectMapper.readTree(response.body());
            String aiResponse = node.path("response").asText("[]").trim();

            // Nettoyer la réponse si elle contient des backticks
            aiResponse = aiResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            // Parser le JSON retourné par l'IA
            JsonNode terms = objectMapper.readTree(aiResponse);
            List<Map<String, String>> result = new ArrayList<>();

            if (terms.isArray()) {
                for (JsonNode termNode : terms) {
                    Map<String, String> termMap = new HashMap<>();
                    termMap.put("term", termNode.path("term").asText(""));
                    termMap.put("definition", termNode.path("definition").asText(""));
                    termMap.put("simpleDefinition", termNode.path("simpleDefinition").asText(""));
                    termMap.put("category", termNode.path("category").asText("Médical"));

                    // Ne pas ajouter si term est vide
                    if (!termMap.get("term").isBlank()) {
                        result.add(termMap);
                    }
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println("Glossary AI error: " + e.getMessage());
            return List.of(); // retourner liste vide si erreur
        }
    }
}