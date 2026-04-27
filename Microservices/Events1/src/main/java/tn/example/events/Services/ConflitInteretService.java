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
import tn.example.events.dto.ConflitAnalyseResult;
import tn.example.events.Entities.Partenariat;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConflitInteretService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final ObjectMapper objectMapper;

    public ConflitAnalyseResult analyserConflitInteret(Partenariat partenariat) {
        String prompt = buildPrompt(partenariat);

        log.info("🔍 Début analyse conflit d'intérêt pour : {}", partenariat.getNomEntreprise());
        log.info("📤 Prompt envoyé à Groq : {}", prompt);

        String url = "https://api.groq.com/openai/v1/chat/completions";

        Map<String, Object> requestBody = Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", List.of(
                        Map.of("role", "system", "content", getSystemPrompt()),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.2,
                "max_tokens", 1500
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
            log.info("📡 Envoi requête à Groq API...");

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            Map responseBody = response.getBody();
            List choices = (List) responseBody.get("choices");
            Map message = (Map) ((Map) choices.get(0)).get("message");
            String content = ((String) message.get("content"))
                    .replaceAll("```json", "").replaceAll("```", "").trim();

            log.info("✅ Réponse reçue de Groq : {}", content);

            ConflitAnalyseResult result = parseContent(content);
            log.info("✅ Analyse terminée — Risque: {} | Recommandation: {}", result.getNiveauRisque(), result.getRecommandation());

            return result;

        } catch (Exception e) {
            log.error("❌ Erreur analyse Groq: {}", e.getMessage());
            log.error("❌ Stack trace complet:", e);
            return ConflitAnalyseResult.builder()
                    .conflitDetecte(false)
                    .niveauRisque("INCONNU")
                    .resume("Analyse indisponible — vérification manuelle requise.")
                    .recommandation("EXAMINER")
                    .justification("Erreur lors de l'analyse automatique.")
                    .conflitsConcurrents(List.of())
                    .controversesMedicales(List.of())
                    .conflitsEthiques(List.of())
                    .build();
        }
    }

    private String getSystemPrompt() {
        return """
            Tu es un expert en conformité et éthique médicale pour une plateforme de pédiatrie-néphrologie (PediaNephro).
            Tu analyses les partenariats potentiels pour détecter des conflits d'intérêt.
            
            Réponds UNIQUEMENT en JSON valide avec cette structure exacte, sans markdown ni texte supplémentaire :
            {
              "conflitDetecte": boolean,
              "niveauRisque": "FAIBLE|MOYEN|ELEVE|CRITIQUE",
              "resume": "résumé court en français",
              "conflitsConcurrents": ["liste des liens avec concurrents détectés"],
              "controversesMedicales": ["liste des controverses médicales connues"],
              "conflitsEthiques": ["liste des conflits éthiques identifiés"],
              "recommandation": "APPROUVER|EXAMINER|REFUSER",
              "justification": "explication détaillée de la recommandation en français"
            }
            
            Critères d'analyse :
            - Concurrents directs de PediaNephro ou plateformes médicales rivales
            - Controverses autour de médicaments pédiatriques ou néphrologiques
            - Conflits d'intérêt éthiques (financement études biaisées, procès, rappels produits)
            - Réputation de l'entreprise dans le domaine médical
            - Pratiques commerciales douteuses dans le secteur santé
            
            Niveau de risque :
            - FAIBLE : aucun signal négatif
            - MOYEN : signaux mineurs, surveillance recommandée
            - ELEVE : conflits avérés, examen approfondi nécessaire
            - CRITIQUE : refus immédiat recommandé
            """;
    }

    private String buildPrompt(Partenariat partenariat) {
        return String.format("""
            Analyse ce partenaire potentiel pour PediaNephro (plateforme médicale spécialisée pédiatrie-néphrologie) :
            
            Nom de l'entreprise : %s
            Email : %s
            Site web : %s
            Téléphone : %s
            Message de collaboration : %s
            Date début collaboration souhaitée : %s
            
            Effectue une analyse approfondie des conflits d'intérêt potentiels.
            """,
                partenariat.getNomEntreprise(),
                partenariat.getEmailEntreprise() != null ? partenariat.getEmailEntreprise() : "Non renseigné",
                partenariat.getSiteWeb() != null ? partenariat.getSiteWeb() : "Non renseigné",
                partenariat.getTelephone() != null ? partenariat.getTelephone() : "Non renseigné",
                partenariat.getMessageCollaboration() != null ? partenariat.getMessageCollaboration() : "Non renseigné",
                partenariat.getDateDebutCollaboration() != null ? partenariat.getDateDebutCollaboration().toString() : "Non renseigné"
        );
    }

    private ConflitAnalyseResult parseContent(String content) throws Exception {
        JsonNode result = objectMapper.readTree(content);
        return ConflitAnalyseResult.builder()
                .conflitDetecte(result.path("conflitDetecte").asBoolean())
                .niveauRisque(result.path("niveauRisque").asText("FAIBLE"))
                .resume(result.path("resume").asText())
                .conflitsConcurrents(parseList(result.path("conflitsConcurrents")))
                .controversesMedicales(parseList(result.path("controversesMedicales")))
                .conflitsEthiques(parseList(result.path("conflitsEthiques")))
                .recommandation(result.path("recommandation").asText("EXAMINER"))
                .justification(result.path("justification").asText())
                .build();
    }

    private List<String> parseList(JsonNode node) {
        if (node.isArray()) {
            return objectMapper.convertValue(node,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        }
        return List.of();
    }
}