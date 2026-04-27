package com.nephroforum.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.Map;

@Service

public class AIService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient ollamaClient = WebClient.create("http://localhost:11434");

    // ── Tag keyword map — enrichi ─────────────────────────────────────────────
    private static final Map<String, List<String>> TAG_KEYWORDS;

    static {
        TAG_KEYWORDS = new HashMap<>();
        // Maladies rénales
        TAG_KEYWORDS.put("kidney", List.of("kidney-health", "renal-function"));
        TAG_KEYWORDS.put("renal", List.of("renal-disease", "kidney-health"));
        TAG_KEYWORDS.put("nephrotic", List.of("nephrotic-syndrome", "proteinuria"));
        TAG_KEYWORDS.put("nephritis", List.of("nephritis", "inflammation"));
        TAG_KEYWORDS.put("ckd", List.of("ckd", "chronic-kidney-disease"));
        TAG_KEYWORDS.put("chronic", List.of("chronic-disease", "long-term-care"));
        TAG_KEYWORDS.put("glomerulo", List.of("glomerulonephritis", "kidney-inflammation"));
        TAG_KEYWORDS.put("polykystique", List.of("polycystic-kidney", "genetic-disease"));
        TAG_KEYWORDS.put("alport", List.of("alport-syndrome", "genetic-nephropathy"));
        TAG_KEYWORDS.put("fsgs", List.of("fsgs", "focal-segmental-glomerulosclerosis"));

        // Symptômes
        TAG_KEYWORDS.put("swelling", List.of("edema", "swelling", "fluid-retention"));
        TAG_KEYWORDS.put("gonflement", List.of("oedeme", "retention-hydrique"));
        TAG_KEYWORDS.put("oedème", List.of("oedeme", "gonflement"));
        TAG_KEYWORDS.put("fatigue", List.of("fatigue", "asthenie"));
        TAG_KEYWORDS.put("pain", List.of("douleur", "pain-management"));
        TAG_KEYWORDS.put("douleur", List.of("douleur", "inconfort"));
        TAG_KEYWORDS.put("headache", List.of("cephalee", "headache"));
        TAG_KEYWORDS.put("nausea", List.of("nausee", "digestif"));
        TAG_KEYWORDS.put("vomit", List.of("vomissement", "digestif"));
        TAG_KEYWORDS.put("urine", List.of("urinalyse", "urine", "miction"));
        TAG_KEYWORDS.put("hématurie", List.of("hematurie", "sang-urine"));
        TAG_KEYWORDS.put("protéinurie", List.of("proteinurie", "albumine"));
        TAG_KEYWORDS.put("protein", List.of("proteinurie", "albumine"));
        TAG_KEYWORDS.put("anemia", List.of("anemie", "hemoglobine"));
        TAG_KEYWORDS.put("anémie", List.of("anemie", "fatigue"));
        TAG_KEYWORDS.put("hypertension", List.of("hypertension", "blood-pressure"));
        TAG_KEYWORDS.put("blood pressure", List.of("tension-arterielle", "hypertension"));
        TAG_KEYWORDS.put("pression", List.of("tension-arterielle", "hypertension"));
        TAG_KEYWORDS.put("infection", List.of("infection-urinaire", "itu"));
        TAG_KEYWORDS.put("itu", List.of("infection-urinaire", "cystite"));

        // Traitements
        TAG_KEYWORDS.put("dialysis", List.of("dialyse", "hemodialyse"));
        TAG_KEYWORDS.put("dialyse", List.of("dialyse", "epuration-extrarenale"));
        TAG_KEYWORDS.put("hemodialyse", List.of("hemodialyse", "dialyse"));
        TAG_KEYWORDS.put("peritoneal", List.of("dialyse-peritoneale", "dialyse"));
        TAG_KEYWORDS.put("transplant", List.of("greffe-rein", "transplantation"));
        TAG_KEYWORDS.put("greffe", List.of("greffe-rein", "transplantation"));
        TAG_KEYWORDS.put("immunosuppresseur", List.of("immunosuppresseur", "traitement"));
        TAG_KEYWORDS.put("cortisone", List.of("corticoides", "traitement"));
        TAG_KEYWORDS.put("corticoide", List.of("corticoides", "immunosuppresseur"));
        TAG_KEYWORDS.put("tacrolimus", List.of("tacrolimus", "immunosuppresseur"));
        TAG_KEYWORDS.put("cyclosporine", List.of("cyclosporine", "immunosuppresseur"));
        TAG_KEYWORDS.put("diuretic", List.of("diuretique", "traitement"));
        TAG_KEYWORDS.put("diurétique", List.of("diuretique", "retention-hydrique"));
        TAG_KEYWORDS.put("epo", List.of("erythropoietine", "anemie"));
        TAG_KEYWORDS.put("erythropoietine", List.of("epo", "anemie"));
        TAG_KEYWORDS.put("plasmapherese", List.of("plasmapherese", "traitement-avance"));
        TAG_KEYWORDS.put("biotherapy", List.of("biotherapie", "traitement-biologique"));

        // Analyses
        TAG_KEYWORDS.put("creatinine", List.of("creatinine", "bilan-renal"));
        TAG_KEYWORDS.put("créatinine", List.of("creatinine", "bilan-renal"));
        TAG_KEYWORDS.put("dfg", List.of("dfg", "filtration-glomerulaire"));
        TAG_KEYWORDS.put("gfr", List.of("dfg", "fonction-renale"));
        TAG_KEYWORDS.put("bilan", List.of("bilan-sanguin", "analyse"));
        TAG_KEYWORDS.put("blood", List.of("bilan-sanguin", "prise-de-sang"));
        TAG_KEYWORDS.put("potassium", List.of("potassium", "electrolytes", "restriction-potassium"));
        TAG_KEYWORDS.put("sodium", List.of("sodium", "electrolytes", "restriction-sel"));
        TAG_KEYWORDS.put("phosphore", List.of("phosphore", "mineraux", "restriction-phosphore", "alimentation"));
        TAG_KEYWORDS.put("calcium", List.of("calcium", "os"));
        TAG_KEYWORDS.put("albumine", List.of("albumine", "proteine"));
        TAG_KEYWORDS.put("hemoglobine", List.of("hemoglobine", "anemie"));
        TAG_KEYWORDS.put("ecographie", List.of("echographie-renale", "imagerie"));
        TAG_KEYWORDS.put("scanner", List.of("scanner", "imagerie"));
        TAG_KEYWORDS.put("irm", List.of("irm", "imagerie"));

        // Procédures
        TAG_KEYWORDS.put("biopsy", List.of("biopsie-renale", "diagnostic"));
        TAG_KEYWORDS.put("biopsie", List.of("biopsie-renale", "procedure"));
        TAG_KEYWORDS.put("catheter", List.of("catheter", "acces-vasculaire"));
        TAG_KEYWORDS.put("fistule", List.of("fistule-arterio-veineuse", "dialyse"));
        TAG_KEYWORDS.put("surgery", List.of("chirurgie", "operation"));
        TAG_KEYWORDS.put("operation", List.of("chirurgie", "procedure"));

        // Nutrition
        TAG_KEYWORDS.put("diet", List.of("regime-alimentaire", "nutrition"));
        TAG_KEYWORDS.put("alimentation", List.of("nutrition", "regime"));
        TAG_KEYWORDS.put("nutrition", List.of("nutrition", "dietetique"));
        TAG_KEYWORDS.put("sel", List.of("restriction-sel", "sodium"));
        TAG_KEYWORDS.put("salt", List.of("restriction-sel", "sodium"));
        TAG_KEYWORDS.put("hydratation", List.of("hydratation", "apport-hydrique"));
        TAG_KEYWORDS.put("water", List.of("hydratation", "apport-liquidien"));
        TAG_KEYWORDS.put("weight", List.of("poids", "surveillance"));
        TAG_KEYWORDS.put("poids", List.of("surveillance-poids", "retention"));

        // Vie quotidienne
        TAG_KEYWORDS.put("school", List.of("scolarite", "vie-scolaire"));
        TAG_KEYWORDS.put("école", List.of("scolarite", "pai"));
        TAG_KEYWORDS.put("sport", List.of("activite-physique", "sport-adapte"));
        TAG_KEYWORDS.put("activity", List.of("activite-physique", "quotidien"));
        TAG_KEYWORDS.put("activité", List.of("activite-physique", "quotidien"));
        TAG_KEYWORDS.put("travel", List.of("voyage", "vie-quotidienne"));
        TAG_KEYWORDS.put("voyage", List.of("voyage", "dialyse-voyage"));
        TAG_KEYWORDS.put("sleep", List.of("sommeil", "repos"));
        TAG_KEYWORDS.put("sommeil", List.of("sommeil", "fatigue"));
        TAG_KEYWORDS.put("stress", List.of("stress", "sante-mentale"));
        TAG_KEYWORDS.put("anxiety", List.of("anxiete", "sante-mentale"));
        TAG_KEYWORDS.put("anxiété", List.of("anxiete", "soutien-psychologique"));
        TAG_KEYWORDS.put("depression", List.of("depression", "sante-mentale"));
        TAG_KEYWORDS.put("soutien", List.of("soutien-psychologique", "accompagnement"));
        TAG_KEYWORDS.put("famille", List.of("famille", "proche-aidant"));
        TAG_KEYWORDS.put("parent", List.of("parents", "proche-aidant"));

        // Croissance enfant
        TAG_KEYWORDS.put("growth", List.of("croissance", "developpement"));
        TAG_KEYWORDS.put("croissance", List.of("croissance", "pediatrie"));
        TAG_KEYWORDS.put("puberty", List.of("puberte", "adolescence"));
        TAG_KEYWORDS.put("adolescent", List.of("adolescence", "pediatrie"));
        TAG_KEYWORDS.put("vaccin", List.of("vaccination", "prevention"));
        TAG_KEYWORDS.put("vaccine", List.of("vaccination", "immunosupprime"));
    }

    // ── Generate description avec Ollama ──────────────────────────────────────
    public String generateDescription(String title) {
        try {
            String prompt = """
                    Tu es un assistant pour un forum médical de néphrologie pédiatrique.
                    Génère une description de post de forum en français, à la première personne,
                    comme si c'était un parent ou patient qui écrit.
                    La description doit être naturelle, entre 80 et 120 mots,
                    poser des questions pertinentes et chercher du soutien.
                    
                    Titre du post : %s
                    
                    Réponds UNIQUEMENT avec la description, sans titre ni introduction.
                    """.formatted(title);

            return callOllama(prompt);
        } catch (Exception e) {
            return generateDescriptionStatic(title);
        }
    }

    // ── Generate tags ─────────────────────────────────────────────────────────
    public List<String> generateTags(String title, String description) {
        String text = (title + " " + description).toLowerCase();
        Set<String> found = new LinkedHashSet<>();

        for (Map.Entry<String, List<String>> entry : TAG_KEYWORDS.entrySet()) {
            if (text.contains(entry.getKey())) {
                found.addAll(entry.getValue());
            }
        }

        if (found.isEmpty()) {
            found.add("nephrologie-pediatrique");
            found.add("sante-renale");
            found.add("conseil-medical");
        }

        return new ArrayList<>(found).subList(0, Math.min(found.size(), 6));
    }

    // ── Résumé IA d'un post ───────────────────────────────────────────────────
    public String summarizePost(String title, String description, int commentCount) {
        try {
            String prompt = """
                    Tu es un assistant médical spécialisé en néphrologie pédiatrique.
                    Fais un résumé clair et concis de ce post de forum en français.
                    Le résumé doit :
                    - Identifier le problème principal en 1 phrase
                    - Mentionner les points clés soulevés
                    - Être entre 60 et 100 mots maximum
                    - Être neutre et factuel
                    
                    Titre : %s
                    Description : %s
                    Nombre de commentaires : %d
                    
                    Réponds UNIQUEMENT avec le résumé, sans introduction.
                    """.formatted(title, description, commentCount);

            return callOllama(prompt);
        } catch (Exception e) {
            return "Résumé indisponible — " + title.substring(0, Math.min(title.length(), 50));
        }
    }

    // ── Appel Ollama ──────────────────────────────────────────────────────────
    private String callOllama(String prompt) {
        Map<String, Object> body = Map.of(
                "model", "mistral",
                "prompt", prompt,
                "stream", false
        );

        Map response = ollamaClient.post()
                .uri("/api/generate")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return response != null ? response.get("response").toString().trim()
                : "Génération indisponible.";
    }

    // ── Description statique fallback ─────────────────────────────────────────
    private String generateDescriptionStatic(String title) {
        String lower = title.toLowerCase();

        if (lower.contains("diet") || lower.contains("alimentation") || lower.contains("nutrition")) {
            return "Je cherche des conseils sur l'alimentation adaptée pour mon enfant atteint " +
                    "d'une maladie rénale. Quels aliments sont autorisés, lesquels éviter ? " +
                    "Avez-vous des idées de repas que les enfants apprécient tout en respectant " +
                    "les restrictions ? Merci de partager vos expériences.";
        }
        if (lower.contains("symptôme") || lower.contains("symptom") || lower.contains("douleur")) {
            return "Mon enfant présente des symptômes inquiétants liés à \"" + title + "\". " +
                    "Je souhaite l'avis de médecins et de familles ayant vécu la même situation. " +
                    "Comment reconnaître ces signes et comment y répondre rapidement ?";
        }
        if (lower.contains("dialyse") || lower.contains("dialysis")) {
            return "Notre famille commence le parcours de la dialyse et nous avons beaucoup " +
                    "de questions. Comment gérez-vous le quotidien avec les séances ? " +
                    "Quels conseils donneriez-vous aux familles qui débutent ce traitement ?";
        }
        if (lower.contains("greffe") || lower.contains("transplant")) {
            return "Nous envisageons une greffe rénale pour notre enfant et nous cherchons " +
                    "des témoignages de familles qui ont vécu cette expérience. " +
                    "Comment s'est passée la préparation, l'opération et le suivi post-greffe ?";
        }
        if (lower.contains("biopsy") || lower.contains("biopsie")) {
            return "Mon enfant doit subir une biopsie rénale et nous sommes anxieux. " +
                    "Comment avez-vous préparé votre enfant à cette procédure ? " +
                    "À quoi s'attendre pendant et après ? Merci pour vos témoignages.";
        }
        if (lower.contains("école") || lower.contains("school") || lower.contains("scolarité")) {
            return "Comment gérez-vous la scolarité de votre enfant avec une maladie rénale ? " +
                    "Avez-vous mis en place un PAI ? Quelles adaptations ont été faites " +
                    "pour que votre enfant puisse suivre normalement sa scolarité ?";
        }
        if (lower.contains("fatigue") || lower.contains("fatigué")) {
            return "Mon enfant souffre d'une fatigue intense liée à sa maladie rénale. " +
                    "Comment gérez-vous cette fatigue au quotidien ? " +
                    "Y a-t-il des stratégies qui ont aidé votre enfant à mieux vivre avec ?";
        }

        return "Je cherche des informations et du soutien concernant \"" + title + "\". " +
                "En tant que parent d'un enfant atteint d'une maladie rénale pédiatrique, " +
                "j'apprécierais les conseils de médecins et de familles ayant vécu des situations similaires. " +
                "Quelles sont les choses les plus importantes à savoir ?";
    }

    // ── Translate avec Ollama/Mistral ─────────────────────────────────────────
    public String translate(String text, String targetLang) {
        try {
            // Découper si le texte est trop long
            int LIMIT = 800;
            if (text.length() <= LIMIT) {
                return translateWithOllama(text, targetLang);
            }

            StringBuilder result = new StringBuilder();
            int start = 0;

            while (start < text.length()) {
                int end = Math.min(start + LIMIT, text.length());
                // Couper proprement à la fin d'une phrase
                if (end < text.length()) {
                    int lastDot = text.lastIndexOf('.', end);
                    if (lastDot > start) end = lastDot + 1;
                }
                String part = text.substring(start, end).trim();
                result.append(translateWithOllama(part, targetLang)).append(" ");
                start = end;
            }

            return result.toString().trim();

        } catch (Exception e) {
            System.out.println("Translation error: " + e.getMessage());
            return text;
        }
    }

    private String translateWithOllama(String text, String targetLang) throws Exception {

        // Map code langue → nom complet pour Mistral
        Map<String, String> langNames = Map.of(
                "fr", "French",
                "en", "English",
                "ar", "Arabic",
                "es", "Spanish",
                "de", "German"
        );
        String langName = langNames.getOrDefault(targetLang.toLowerCase(), targetLang);

        // Prompt clair pour Mistral
        String prompt = String.format(
                "Translate the following medical text to %s. " +
                        "Return ONLY the translated text, no explanations, no notes, no original text.\n\n%s",
                langName, text
        );

        // Construire le body JSON pour Ollama
        String requestBody = String.format(
                "{\"model\": \"mistral\", \"prompt\": %s, \"stream\": false}",
                objectMapper.writeValueAsString(prompt)
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        // Parser la réponse Ollama
        JsonNode node = objectMapper.readTree(response.body());
        String translated = node.path("response").asText("").trim();

        if (translated.isBlank()) return text;

        return translated;
    }
}