package tn.example.events.Services;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.example.events.dto.PubMedPublication;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class PubMedService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String ESEARCH_URL =
            "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi" +
                    "?db=pubmed&retmax=3&retmode=json&sort=date&term=";

    private static final String ESUMMARY_URL =
            "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi" +
                    "?db=pubmed&retmode=json&id=";

    private static final String PUBMED_BASE =
            "https://pubmed.ncbi.nlm.nih.gov/";

    public List<PubMedPublication> rechercherPublications(
            String nomEvent, String description) {
        try {
            // Étape 1 — Extraire mots-clés
            String query = buildQuery(nomEvent, description);
            log.info("🔬 PubMed query : {}", query);

            // Étape 2 — Récupérer les IDs
            String searchUrl = ESEARCH_URL +
                    java.net.URLEncoder.encode(query, "UTF-8");
            String searchResponse = restTemplate.getForObject(
                    searchUrl, String.class);

            JsonNode searchRoot = objectMapper.readTree(searchResponse);
            JsonNode idList = searchRoot
                    .path("esearchresult").path("idlist");

            if (!idList.isArray() || idList.size() == 0) {
                log.warn("⚠️ Aucun résultat PubMed pour : {}", query);
                return List.of();
            }

            // Étape 3 — Construire liste IDs
            List<String> ids = StreamSupport
                    .stream(idList.spliterator(), false)
                    .map(JsonNode::asText)
                    .collect(Collectors.toList());

            log.info("✅ IDs trouvés : {}", ids);

            // Étape 4 — Récupérer détails
            String summaryUrl = ESUMMARY_URL + String.join(",", ids);
            String summaryResponse = restTemplate.getForObject(
                    summaryUrl, String.class);

            return parsePublications(summaryResponse, ids);

        } catch (Exception e) {
            log.error("❌ Erreur PubMed : {}", e.getMessage());
            return List.of();
        }
    }

    private String buildQuery(String nomEvent, String description) {
        // Mots-clés médicaux extraits du nom + description
        String combined = (nomEvent + " " +
                (description != null ? description : ""))
                .toLowerCase();

        // Détection thèmes médicaux
        if (combined.contains("nephro") ||
                combined.contains("rein") ||
                combined.contains("renal")) {
            return "pediatric nephrology kidney disease";
        }
        if (combined.contains("diabete") ||
                combined.contains("diabetes")) {
            return "pediatric diabetes mellitus";
        }
        if (combined.contains("cardio") ||
                combined.contains("coeur") ||
                combined.contains("heart")) {
            return "pediatric cardiology";
        }
        if (combined.contains("cancer") ||
                combined.contains("oncol")) {
            return "pediatric oncology";
        }
        if (combined.contains("vaccin") ||
                combined.contains("immun")) {
            return "pediatric immunology vaccination";
        }
        if (combined.contains("neurol") ||
                combined.contains("cerveau")) {
            return "pediatric neurology";
        }

        // Par défaut — thème général PediaNephro
        return "pediatric nephrology " +
                nomEvent.replaceAll("[^a-zA-Z0-9 ]", "")
                        .trim()
                        .replaceAll("\\s+", "+");
    }

    private List<PubMedPublication> parsePublications(
            String response, List<String> ids) throws Exception {

        JsonNode root = objectMapper.readTree(response);
        JsonNode result = root.path("result");

        List<PubMedPublication> publications = new ArrayList<>();

        for (String id : ids) {
            JsonNode article = result.path(id);
            if (article.isMissingNode()) continue;

            // Titre
            String titre = article.path("title").asText("Sans titre");

            // Journal
            String journal = article.path("fulljournalname")
                    .asText(article.path("source").asText("Journal inconnu"));

            // Année
            String pubDate = article.path("pubdate").asText("");
            String annee = pubDate.length() >= 4 ?
                    pubDate.substring(0, 4) : pubDate;

            // Auteurs
            JsonNode auteursNode = article.path("authors");
            String auteurs = "Auteurs inconnus";
            if (auteursNode.isArray() && auteursNode.size() > 0) {
                List<String> noms = new ArrayList<>();
                for (int i = 0; i < Math.min(3, auteursNode.size()); i++) {
                    noms.add(auteursNode.get(i).path("name").asText());
                }
                auteurs = String.join(", ", noms);
                if (auteursNode.size() > 3) auteurs += " et al.";
            }

            // DOI
            JsonNode articleIds = article.path("articleids");
            String doi = "";
            if (articleIds.isArray()) {
                for (JsonNode aid : articleIds) {
                    if ("doi".equals(aid.path("idtype").asText())) {
                        doi = aid.path("value").asText("");
                        break;
                    }
                }
            }

            publications.add(PubMedPublication.builder()
                    .pmid(id)
                    .titre(titre)
                    .journal(journal)
                    .annee(annee)
                    .auteurs(auteurs)
                    .lienPubMed(PUBMED_BASE + id)
                    .doi(doi)
                    .build());
        }

        return publications;
    }
}
