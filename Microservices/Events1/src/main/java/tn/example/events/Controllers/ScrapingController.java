package tn.example.events.Controllers;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/scraping")
/*@CrossOrigin(origins = "http://localhost:4200")*/
@RequiredArgsConstructor
public class ScrapingController {

    @GetMapping("/entreprise")
    public ResponseEntity<Map<String, String>> scrapeEntreprise(
            @RequestParam String url) {

        Map<String, String> result = new HashMap<>();
        result.put("email", "");
        result.put("telephone", "");

        // ✅ Ajoute https:// si manquant
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .followRedirects(true)  // ✅ suit les redirections
                    .ignoreHttpErrors(true) // ✅ ignore les erreurs HTTP
                    .get();

            String pageText = doc.text() + " " + doc.html();

            // ✅ Priorité aux balises mailto et tel
            doc.select("a[href^=mailto]").forEach(el -> {
                String mailto = el.attr("href").replace("mailto:", "").split("\\?")[0].trim();
                if (!mailto.isEmpty() && result.get("email").isEmpty()) {
                    result.put("email", mailto);
                }
            });

            doc.select("a[href^=tel]").forEach(el -> {
                String tel = el.attr("href").replace("tel:", "").trim();
                if (!tel.isEmpty() && result.get("telephone").isEmpty()) {
                    result.put("telephone", tel);
                }
            });

            // ✅ Regex Email si pas trouvé via mailto
            if (result.get("email").isEmpty()) {
                Pattern emailPattern = Pattern.compile(
                        "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}"
                );
                Matcher emailMatcher = emailPattern.matcher(pageText);
                while (emailMatcher.find()) {
                    String email = emailMatcher.group();
                    if (!email.contains(".png") && !email.contains(".jpg")
                            && !email.contains(".css") && !email.contains(".js")
                            && !email.contains("example") && !email.contains("sentry")) {
                        result.put("email", email);
                        break;
                    }
                }
            }

            // ✅ Regex Téléphone si pas trouvé via tel:
            if (result.get("telephone").isEmpty()) {
                Pattern phonePattern = Pattern.compile(
                        "(\\+?\\d{1,3}[\\s.-]?)?(\\(?\\d{2,3}\\)?[\\s.-]?){2,4}\\d{2,4}"
                );
                Matcher phoneMatcher = phonePattern.matcher(pageText);
                while (phoneMatcher.find()) {
                    String phone = phoneMatcher.group().trim();
                    if (phone.length() >= 8 && phone.length() <= 20) {
                        result.put("telephone", phone);
                        break;
                    }
                }
            }

        } catch (Exception e) {
            result.put("error", "Impossible d'accéder au site : " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}
