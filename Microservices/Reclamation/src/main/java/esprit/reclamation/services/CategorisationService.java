package esprit.reclamation.services;

import esprit.reclamation.entities.CategorieReclamation;
import esprit.reclamation.entities.Priorite;
import esprit.reclamation.entities.PrioriteReclamation;
import esprit.reclamation.entities.Reclamation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@Slf4j
public class CategorisationService {

    public void enrichir(Reclamation rec) {
        String titre = rec.getTitre() != null ? rec.getTitre() : "";
        String desc = rec.getDescription() != null ? rec.getDescription() : "";
        String texte = normalize(titre + " " + desc);

        CategorieReclamation categorie = detecterCategorie(texte);
        rec.setCategorie(categorie);

        PrioriteReclamation pr = detecterPriorite(texte, categorie);
        rec.setPrioriteReclamation(pr);

        alignerPrioriteLegacy(rec);

        LocalDateTime base = LocalDateTime.now();
        rec.setSlaDeadline(base.plusHours(heuresSla(categorie)));

        log.debug("Réclamation enrichie : catégorie={}, prioritéSLA={}, slaDeadline={}", categorie, pr, rec.getSlaDeadline());
    }

    private static String normalize(String s) {
        return s.toLowerCase(Locale.FRENCH).replace('\u00a0', ' ');
    }

    private static CategorieReclamation detecterCategorie(String t) {
        if (contient(t, "facture", "facturation", "paiement", "remboursement", "tarif", "prix", "cotisation")) {
            return CategorieReclamation.FACTURATION;
        }
        if (contient(t, "bug", "erreur", "technique", "lent", "lenteur", "chargement", "page blanche", "plantage", "planté", "plante")) {
            return CategorieReclamation.TECHNIQUE;
        }
        if (contient(t, "qualité", "qualite", "accueil", "délai", "delai", "insult", "mécontent", "mecontent")) {
            return CategorieReclamation.QUALITE_SERVICE;
        }
        if (contient(t, "compte", "mot de passe", "acces", "accès", "bloqué", "bloque", "verrouill", "2fa", "authentification")) {
            return CategorieReclamation.ACCES_COMPTE;
        }
        return CategorieReclamation.AUTRE;
    }

    private static boolean contient(String texte, String... cles) {
        for (String c : cles) {
            if (texte.contains(c)) {
                return true;
            }
        }
        return false;
    }

    private static PrioriteReclamation detecterPriorite(String t, CategorieReclamation categorie) {
        if (contient(t, "urgent", "critique", "bloqué", "bloque", "bloquée", "bloquee")) {
            return PrioriteReclamation.CRITIQUE;
        }
        if (contient(t, "important") || categorie == CategorieReclamation.ACCES_COMPTE) {
            return PrioriteReclamation.HAUTE;
        }
        return PrioriteReclamation.NORMALE;
    }

    private static int heuresSla(CategorieReclamation categorie) {
        return switch (categorie) {
            case FACTURATION -> 24;
            case ACCES_COMPTE -> 12;
            case TECHNIQUE -> 48;
            case QUALITE_SERVICE, AUTRE -> 72;
        };
    }

    private static void alignerPrioriteLegacy(Reclamation r) {
        PrioriteReclamation pr = r.getPrioriteReclamation();
        if (pr == null) {
            return;
        }
        Priorite legacy = switch (pr) {
            case CRITIQUE -> Priorite.URGENTE;
            case HAUTE -> Priorite.ELEVEE;
            case BASSE -> Priorite.FAIBLE;
            case NORMALE -> Priorite.MOYENNE;
        };
        r.setPriorite(legacy);
    }
}
