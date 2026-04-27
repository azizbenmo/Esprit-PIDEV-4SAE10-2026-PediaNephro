package esprit.User.security;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Détection heuristique de motifs d'injection SQL sur les champs login.
 * Complète l'usage de requêtes paramétrées côté persistance : bloque les payloads évidents
 * et permet d'alerter les administrateurs sans exposer le détail à l'attaquant.
 */
public final class SqlInjectionDetector {

    private SqlInjectionDetector() {
    }

    private static final List<Pattern> PATTERNS = List.of(
            Pattern.compile("(?i)('|(%27))\\s*or\\s*(')?\\d*'?\\s*=\\s*'?\\d*"),
            Pattern.compile("(?i)\\bor\\s+\\d+\\s*=\\s*\\d+"),
            Pattern.compile("(?i)('|(%27))\\s*or\\s*('|(%27))"),
            Pattern.compile("(?i)union(\\s+all)?\\s+select"),
            Pattern.compile("(?i);\\s*(drop|delete|truncate|insert|update|alter)\\s+"),
            Pattern.compile("(?i)/\\*"),
            Pattern.compile("(?i)--\\s*(or|and|select|union)"),
            Pattern.compile("(?i)\\bxp_cmdshell\\b"),
            Pattern.compile("(?i)into\\s+outfile"),
            Pattern.compile("(?i)\\bsleep\\s*\\("),
            Pattern.compile("(?i)\\bbenchmark\\s*\\("),
            Pattern.compile("(?i)information_schema\\."),
            Pattern.compile("(?i)load_file\\s*\\("),
            Pattern.compile("(?i)\\bexec\\s*\\("),
            Pattern.compile("(?i)waitfor\\s+delay")
    );

    /**
     * @param inputs champs à contrôler (ex. identifiant + mot de passe)
     * @return true si au moins un motif suspect est détecté
     */
    public static boolean looksMalicious(String... inputs) {
        if (inputs == null) {
            return false;
        }
        for (String raw : inputs) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String normalized = raw.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
            for (Pattern p : PATTERNS) {
                if (p.matcher(normalized).find()) {
                    return true;
                }
            }
        }
        return false;
    }
}
