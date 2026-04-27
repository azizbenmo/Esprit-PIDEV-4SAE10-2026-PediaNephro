package tn.example.events.Services;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class AIDescriptionService {

    public String generateDescription(String eventName) {

        // ✅ Simulation avec délai (comme une vraie API)
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String name = eventName.toLowerCase();

        // Descriptions intelligentes basées sur les mots-clés

        if (name.contains("conférence") || name.contains("conference")) {
            return "Cette conférence internationale réunit les meilleurs spécialistes " +
                    "de néphrologie pédiatrique pour partager leurs connaissances et expériences cliniques. " +
                    "Au programme : présentations scientifiques de pointe, ateliers pratiques interactifs et " +
                    "tables rondes sur les innovations thérapeutiques en santé rénale infantile. " +
                    "Un rendez-vous incontournable pour tous les professionnels de santé dédiés à l'amélioration " +
                    "de la prise en charge des pathologies rénales chez l'enfant.";
        }

        if (name.contains("formation")) {
            return "Formation continue intensive destinée aux professionnels de santé spécialisés " +
                    "en néphrologie pédiatrique. Cette session complète aborde les protocoles de diagnostic modernes, " +
                    "les stratégies thérapeutiques innovantes et la gestion multidisciplinaire des pathologies rénales " +
                    "chez l'enfant. Les participants développeront leurs compétences cliniques à travers des cas " +
                    "pratiques, des démonstrations techniques et des échanges avec des experts reconnus du domaine.";
        }

        if (name.contains("séminaire") || name.contains("seminaire")) {
            return "Séminaire scientifique dédié aux dernières avancées en recherche néphrologique pédiatrique. " +
                    "Chercheurs et cliniciens de renommée internationale présenteront leurs travaux récents sur " +
                    "les maladies rénales infantiles, favorisant les collaborations et l'innovation dans le domaine. " +
                    "Un espace d'échange privilégié pour découvrir les perspectives thérapeutiques futures et " +
                    "les protocoles de recherche en cours dans les centres d'excellence.";
        }

        if (name.contains("atelier")) {
            return "Atelier pratique interactif centré sur les techniques et protocoles spécifiques " +
                    "en néphrologie pédiatrique. Une opportunité unique pour les professionnels de santé " +
                    "de développer leurs compétences cliniques et techniques à travers des démonstrations en direct, " +
                    "des cas cliniques commentés et des mises en situation pratiques. " +
                    "Format participatif favorisant l'apprentissage par la pratique et l'échange entre pairs.";
        }

        if (name.contains("colloque")) {
            return "Colloque multidisciplinaire réunissant néphrologues pédiatres, chercheurs, infirmières " +
                    "spécialisées et professionnels paramédicaux autour des défis actuels en santé rénale infantile. " +
                    "Un espace d'échange privilégié pour construire ensemble les protocoles de soins de demain, " +
                    "partager les expériences cliniques et améliorer la qualité de vie des jeunes patients " +
                    "et de leurs familles dans une approche holistique et centrée sur le patient.";
        }

        if (name.contains("congrès") || name.contains("congres")) {
            return "Congrès scientifique majeur rassemblant l'ensemble de la communauté néphrologique pédiatrique " +
                    "nationale et internationale. Quatre jours d'immersion totale dans l'actualité de la spécialité " +
                    "avec sessions plénières, communications orales, posters scientifiques et symposiums industriels. " +
                    "Une plateforme d'excellence pour découvrir les innovations, tisser des liens professionnels " +
                    "et construire les réseaux de collaboration de demain.";
        }

        if (name.contains("journée") || name.contains("journee")) {
            return "Journée d'étude consacrée aux actualités et pratiques cliniques en néphrologie pédiatrique. " +
                    "Format condensé alternant présentations théoriques, retours d'expérience et discussions interactives " +
                    "autour des problématiques quotidiennes rencontrées par les soignants. " +
                    "Une opportunité de mise à jour des connaissances et d'échanges constructifs " +
                    "dans une ambiance conviviale et propice au networking professionnel.";
        }

        if (name.contains("symposium")) {
            return "Symposium scientifique de haut niveau réunissant les leaders d'opinion internationaux " +
                    "en néphrologie pédiatrique. Focus sur une thématique précise avec analyses approfondies, " +
                    "débats contradictoires et synthèses des recommandations actuelles. " +
                    "Format favorisant les échanges de qualité entre experts et praticiens pour faire évoluer " +
                    "les standards de prise en charge et anticiper les enjeux de santé publique de demain.";
        }

        if (name.contains("greffe") || name.contains("transplantation")) {
            return "Événement spécialisé centré sur la transplantation rénale pédiatrique. " +
                    "Aborde l'ensemble du parcours du jeune greffé : bilan pré-greffe, aspects chirurgicaux, " +
                    "suivi immunologique, gestion des complications et qualité de vie à long terme. " +
                    "Réunit chirurgiens, néphrologues, immunologistes et équipes de coordination pour " +
                    "optimiser la prise en charge globale et améliorer les taux de succès des greffes.";
        }

        if (name.contains("dialyse")) {
            return "Session dédiée aux techniques et enjeux de la dialyse en pédiatrie. " +
                    "Couvre l'hémodialyse et la dialyse péritonéale avec leurs spécificités techniques, " +
                    "la gestion des accès vasculaires, la prévention des complications et l'accompagnement " +
                    "psychosocial des enfants dialysés et de leurs familles. " +
                    "Approche pratique et multidisciplinaire pour optimiser la prise en charge quotidienne.";
        }

        if (name.contains("recherche")) {
            return "Rencontre scientifique axée sur la recherche fondamentale et translationnelle " +
                    "en néphrologie pédiatrique. Présentation des projets en cours, méthodologies innovantes " +
                    "et résultats préliminaires dans les domaines de la génétique, de l'immunologie et " +
                    "de la pharmacologie rénale infantile. Objectif : favoriser les synergies entre laboratoires " +
                    "et services cliniques pour accélérer le passage de la recherche au lit du patient.";
        }

        if (name.contains("urgence")) {
            return "Formation intensive aux urgences néphrologiques pédiatriques. " +
                    "Gestion des situations critiques : insuffisance rénale aiguë, troubles électrolytiques sévères, " +
                    "complications de dialyse et urgences post-transplantation. " +
                    "Approche pratique avec algorithmes décisionnels, cas cliniques et simulations " +
                    "pour améliorer la réactivité et la qualité de prise en charge en contexte d'urgence.";
        }

        if (name.contains("nutrition")) {
            return "Symposium dédié aux aspects nutritionnels dans les pathologies rénales pédiatriques. " +
                    "Explore les besoins spécifiques des enfants insuffisants rénaux, les régimes adaptés selon " +
                    "les stades de la maladie, la gestion de la croissance et la prévention des carences. " +
                    "Collaboration étroite entre néphrologues et nutritionnistes pour élaborer des protocoles " +
                    "nutritionnels optimisés et améliorer la qualité de vie des jeunes patients.";
        }

        if (name.contains("rare")) {
            return "Focus sur les maladies rénales rares en pédiatrie. Présentation des pathologies orphelines, " +
                    "challenges diagnostiques, thérapies innovantes et réseaux de soins spécialisés. " +
                    "Partage d'expériences entre centres experts pour améliorer l'errance diagnostique, " +
                    "accélérer l'accès aux traitements et renforcer l'accompagnement des familles " +
                    "confrontées à ces situations complexes et souvent isolantes.";
        }

        // ✅ Description par défaut (générique mais de qualité)
        return "Événement médical de néphrologie pédiatrique rassemblant des experts, " +
                "chercheurs et professionnels de santé autour des dernières avancées en santé rénale infantile. " +
                "Programme riche combinant présentations scientifiques, ateliers pratiques et moments d'échanges " +
                "pour favoriser le partage de connaissances et l'amélioration continue des pratiques cliniques. " +
                "Une occasion privilégée de formation continue et de networking professionnel dans un cadre " +
                "propice aux collaborations interdisciplinaires et à l'innovation thérapeutique.";
    }
}

