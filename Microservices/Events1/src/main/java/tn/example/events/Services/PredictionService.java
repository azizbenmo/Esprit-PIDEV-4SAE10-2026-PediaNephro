package tn.example.events.Services;


import org.springframework.stereotype.Service;
import tn.example.events.Entities.Event;
import tn.example.events.Entities.Statut;
import tn.example.events.Repositories.EventRepository;
import tn.example.events.Repositories.InscriptionRepository;

import java.time.Month;
import java.util.List;

@Service
public class PredictionService {

    private final EventRepository eventRepository;
    private final InscriptionRepository inscriptionRepository;

    public PredictionService(EventRepository eventRepository,
                             InscriptionRepository inscriptionRepository) {
        this.eventRepository = eventRepository;
        this.inscriptionRepository = inscriptionRepository;
    }

    public PredictionResult predict(Event targetEvent) {

        // Récupère tous les événements archivés (passés)
        List<Event> eventsPasses = eventRepository
                .findByArchiveTrue()
                .stream()
                .filter(e -> e.getCapacite() != null && e.getCapacite() > 0)
                .toList();

        if (eventsPasses.isEmpty()) {
            return new PredictionResult(0, "INSUFFISANT",
                    "Pas assez de données historiques pour prédire");
        }

        double score = 0;
        int facteurs = 0;

        // FACTEUR 1 — Taux de remplissage moyen global (40%)
        double tauxMoyen = eventsPasses.stream()
                .mapToDouble(e -> {
                    long confirmes = inscriptionRepository
                            .countByEventAndStatut(e, Statut.CONFIRME);
                    return (double) confirmes / e.getCapacite();
                })
                .average()
                .orElse(0);

        score += tauxMoyen * 40;
        facteurs++;

        // FACTEUR 2 — Même lieu (25%)
        if (targetEvent.getLieu() != null) {
            String lieuTarget = targetEvent.getLieu().toLowerCase();
            String premierMot = lieuTarget.split(" ")[0];

            List<Event> memeLieu = eventsPasses.stream()
                    .filter(e -> e.getLieu() != null &&
                            e.getLieu().toLowerCase().contains(premierMot))
                    .toList();

            if (!memeLieu.isEmpty()) {
                double tauxLieu = memeLieu.stream()
                        .mapToDouble(e -> {
                            long confirmes = inscriptionRepository
                                    .countByEventAndStatut(e, Statut.CONFIRME);
                            return (double) confirmes / e.getCapacite();
                        })
                        .average()
                        .orElse(0);
                score += tauxLieu * 25;
                facteurs++;
            }
        }

        // FACTEUR 3 — Même mois / saison (20%)
        if (targetEvent.getDateDebut() != null) {
            Month moisTarget = targetEvent.getDateDebut().getMonth();
            List<Event> memeMois = eventsPasses.stream()
                    .filter(e -> e.getDateDebut() != null &&
                            e.getDateDebut().getMonth() == moisTarget)
                    .toList();

            if (!memeMois.isEmpty()) {
                double tauxMois = memeMois.stream()
                        .mapToDouble(e -> {
                            long confirmes = inscriptionRepository
                                    .countByEventAndStatut(e, Statut.CONFIRME);
                            return (double) confirmes / e.getCapacite();
                        })
                        .average()
                        .orElse(0);
                score += tauxMois * 20;
                facteurs++;
            }
        }

        // FACTEUR 4 — Capacité similaire (15%)
        if (targetEvent.getCapacite() != null) {
            int cap = targetEvent.getCapacite();
            List<Event> memeCapacite = eventsPasses.stream()
                    .filter(e -> e.getCapacite() != null &&
                            Math.abs(e.getCapacite() - cap) <= cap * 0.3)
                    .toList();

            if (!memeCapacite.isEmpty()) {
                double tauxCap = memeCapacite.stream()
                        .mapToDouble(e -> {
                            long confirmes = inscriptionRepository
                                    .countByEventAndStatut(e, Statut.CONFIRME);
                            return (double) confirmes / e.getCapacite();
                        })
                        .average()
                        .orElse(0);
                score += tauxCap * 15;
                facteurs++;
            }
        }

        // Normalise le score sur 100
        int scoreNormalise = (int) Math.min(100, Math.round(score));

        String niveau;
        String message;

        if (eventsPasses.size() < 3) {
            niveau = "INSUFFISANT";
            message = "Données insuffisantes (" + eventsPasses.size() + " événement(s) analysé(s)). Ajoutez plus d'événements historiques.";
        } else if (scoreNormalise >= 80) {
            niveau = "SOLD_OUT";
            message = "Sold-out probable — événements similaires se remplissent très rapidement";
        } else if (scoreNormalise >= 60) {
            niveau = "FORTE";
            message = "Forte demande prévue — remplissage rapide attendu";
        } else if (scoreNormalise >= 30) {
            niveau = "MODEREE";
            message = "Demande modérée — remplissage progressif attendu";
        } else {
            niveau = "FAIBLE";
            message = "Demande faible — places probablement disponibles jusqu'à l'événement";
        }

        return new PredictionResult(scoreNormalise, niveau, message);
    }

    // Classe résultat
    public static class PredictionResult {
        public int score;
        public String niveau;
        public String message;

        public PredictionResult(int score, String niveau, String message) {
            this.score = score;
            this.niveau = niveau;
            this.message = message;
        }

        // Getters
        public int getScore() { return score; }
        public String getNiveau() { return niveau; }
        public String getMessage() { return message; }
    }
}
