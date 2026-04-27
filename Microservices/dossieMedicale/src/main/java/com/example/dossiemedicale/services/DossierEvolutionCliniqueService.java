package com.example.dossiemedicale.services;

import com.example.dossiemedicale.DTO.AnalyseEvolutionResponse;
import com.example.dossiemedicale.DTO.ConstanteEvolutionItem;
import com.example.dossiemedicale.entities.Alerte;
import com.example.dossiemedicale.entities.ConstanteVitale;
import com.example.dossiemedicale.entities.DossierMedical;
import com.example.dossiemedicale.entities.Enfant;
import com.example.dossiemedicale.entities.Examen;
import com.example.dossiemedicale.entities.Hospitalisation;
import com.example.dossiemedicale.repositoories.AlerteRepository;
import com.example.dossiemedicale.repositoories.ConstanteVitaleRepository;
import com.example.dossiemedicale.repositoories.DossierMedicalRepository;
import com.example.dossiemedicale.repositoories.ExamenRepository;
import com.example.dossiemedicale.repositoories.HospitalisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DossierEvolutionCliniqueService {

    private final DossierMedicalRepository dossierMedicalRepository;
    private final ConstanteVitaleRepository constanteVitaleRepository;
    private final AlerteRepository alerteRepository;
    private final ExamenRepository examenRepository;
    private final HospitalisationRepository hospitalisationRepository;

    public AnalyseEvolutionResponse analyserEvolution(Long dossierId) {

        DossierMedical dossier = dossierMedicalRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier médical introuvable"));

        Enfant enfant = dossier.getEnfant();

        List<ConstanteVitale> constantes = constanteVitaleRepository
                .findTop20ByDossier_IdDossierOrderByDateMesureDesc(dossierId);

        List<Alerte> alertes = alerteRepository
                .findTop20ByConstante_Dossier_IdDossierOrderByDateDeclenchementDesc(dossierId);

        List<Examen> examens = examenRepository
                .findTop20ByDossier_IdDossierOrderByDateExamenDesc(dossierId);

        List<Hospitalisation> hospitalisations = hospitalisationRepository
                .findTop20ByEnfant_IdEnfantOrderByDateEntreeDesc(enfant.getIdEnfant());

        Map<String, List<ConstanteVitale>> constantesParType = constantes.stream()
                .filter(c -> c.getType() != null)
                .collect(Collectors.groupingBy(ConstanteVitale::getType));

        List<ConstanteEvolutionItem> constantesAnalysees = new ArrayList<>();
        List<String> pointsPositifs = new ArrayList<>();
        List<String> pointsVigilance = new ArrayList<>();

        int scoreVigilance = 0;
        int nbTypesInstables = 0;

        for (Map.Entry<String, List<ConstanteVitale>> entry : constantesParType.entrySet()) {
            String type = entry.getKey();
            List<ConstanteVitale> liste = entry.getValue().stream()
                    .sorted(Comparator.comparing(ConstanteVitale::getDateMesure))
                    .collect(Collectors.toList());

            ConstanteEvolutionItem item = analyserConstante(type, liste);
            constantesAnalysees.add(item);

            if ("STABLE".equals(item.getTendance())) {
                pointsPositifs.add(normaliserType(type) + " globalement stable.");
            } else {
                pointsVigilance.add("Évolution à surveiller pour " + normaliserType(type) + ".");
                nbTypesInstables++;
            }

            if ("BAS".equalsIgnoreCase(item.getStatutActuel()) || "HAUT".equalsIgnoreCase(item.getStatutActuel())) {
                scoreVigilance += 2;
            }

            if ("VARIABLE".equals(item.getTendance()) || "ANORMALE".equals(item.getTendance())) {
                scoreVigilance += 2;
            }
        }

        if (!alertes.isEmpty()) {
            pointsVigilance.add("Des alertes cliniques ont été enregistrées dans le dossier.");
            scoreVigilance += 2;
        }

        if (alertes.size() >= 3) {
            pointsVigilance.add("Répétition d’alertes nécessitant une attention renforcée.");
            scoreVigilance += 2;
        }

        if (!hospitalisations.isEmpty()) {
            pointsVigilance.add("Un antécédent d’hospitalisation est retrouvé.");
            scoreVigilance += 3;
        }

        if (examens.size() >= 3) {
            pointsPositifs.add("Le dossier comporte des examens biologiques récents contribuant au suivi clinique.");
        }

        String niveauVigilance = calculerNiveauVigilance(scoreVigilance);
        String tendanceGenerale = calculerTendanceGenerale(nbTypesInstables, alertes.size(), hospitalisations.size());
        String conclusion = genererConclusion(niveauVigilance, tendanceGenerale);

        return AnalyseEvolutionResponse.builder()
                .idDossier(dossierId)
                .niveauVigilance(niveauVigilance)
                .tendanceGenerale(tendanceGenerale)
                .pointsPositifs(pointsPositifs)
                .pointsVigilance(pointsVigilance)
                .constantesAnalysees(constantesAnalysees)
                .conclusion(conclusion)
                .build();
    }

    private ConstanteEvolutionItem analyserConstante(String type, List<ConstanteVitale> liste) {
        if (liste == null || liste.isEmpty()) {
            return ConstanteEvolutionItem.builder()
                    .type(type)
                    .tendance("INCONNUE")
                    .interpretation("Aucune donnée disponible.")
                    .derniereValeur(null)
                    .statutActuel("INCONNU")
                    .build();
        }

        ConstanteVitale derniere = liste.get(liste.size() - 1);
        String statutActuel = calculerStatut(derniere);

        int nbBas = 0;
        int nbHaut = 0;
        int nbNormal = 0;

        for (ConstanteVitale c : liste) {
            String statut = calculerStatut(c);
            if ("BAS".equals(statut)) nbBas++;
            else if ("HAUT".equals(statut)) nbHaut++;
            else if ("NORMAL".equals(statut)) nbNormal++;
        }

        String tendance;
        String interpretation;

        if (nbBas > 0 && nbHaut > 0) {
            tendance = "VARIABLE";
            interpretation = "Alternance de valeurs basses et élevées au cours du suivi.";
        } else if (nbHaut > 0 && nbNormal > 0) {
            tendance = "ANORMALE";
            interpretation = "Présence répétée de valeurs au-dessus de l’intervalle de référence.";
        } else if (nbBas > 0 && nbNormal > 0) {
            tendance = "ANORMALE";
            interpretation = "Présence répétée de valeurs en dessous de l’intervalle de référence.";
        } else if (liste.size() >= 2) {
            double premiereValeur = valeurOuZero(liste.get(0).getValeur());
            double derniereValeur = valeurOuZero(derniere.getValeur());

            if (derniereValeur > premiereValeur) {
                tendance = "EN_HAUSSE";
                interpretation = "Tendance évolutive à la hausse sur les données disponibles.";
            } else if (derniereValeur < premiereValeur) {
                tendance = "EN_BAISSE";
                interpretation = "Tendance évolutive à la baisse sur les données disponibles.";
            } else {
                tendance = "STABLE";
                interpretation = "Valeurs globalement stables sur la période observée.";
            }
        } else {
            tendance = "STABLE";
            interpretation = "Donnée ponctuelle sans variation significative objectivable.";
        }

        if (nbNormal == liste.size()) {
            tendance = "STABLE";
            interpretation = "Valeurs globalement dans l’intervalle de référence.";
        }

        return ConstanteEvolutionItem.builder()
                .type(type)
                .tendance(tendance)
                .interpretation(interpretation)
                .derniereValeur(derniere.getValeur())
                .statutActuel(statutActuel)
                .build();
    }

    private String calculerStatut(ConstanteVitale c) {
        if (c == null || c.getValeur() == null) return "INCONNU";
        if (c.getSeuilMin() != null && c.getValeur() < c.getSeuilMin()) return "BAS";
        if (c.getSeuilMax() != null && c.getValeur() > c.getSeuilMax()) return "HAUT";
        return "NORMAL";
    }

    private double valeurOuZero(Double valeur) {
        return valeur != null ? valeur : 0.0;
    }

    private String calculerNiveauVigilance(int score) {
        if (score >= 6) return "ELEVE";
        if (score >= 3) return "MODERE";
        return "FAIBLE";
    }

    private String calculerTendanceGenerale(int nbTypesInstables, int nbAlertes, int nbHospitalisations) {
        if (nbTypesInstables >= 2 || nbAlertes >= 3 || nbHospitalisations > 0) {
            return "INSTABLE";
        }
        if (nbTypesInstables == 1 || nbAlertes > 0) {
            return "SURVEILLANCE";
        }
        return "STABLE";
    }

    private String genererConclusion(String niveauVigilance, String tendanceGenerale) {
        if ("ELEVE".equals(niveauVigilance)) {
            return "L’évolution clinique apparaît instable, marquée par plusieurs éléments de vigilance, justifiant une surveillance médicale renforcée.";
        }
        if ("MODERE".equals(niveauVigilance)) {
            return "L’évolution clinique met en évidence certains éléments de vigilance nécessitant un suivi régulier et une corrélation au contexte clinique.";
        }
        return "L’évolution clinique du dossier apparaît globalement stable sur les données actuellement disponibles.";
    }

    private String normaliserType(String type) {
        if (type == null || type.isBlank()) return "la constante concernée";

        return switch (type.toUpperCase()) {
            case "TEMPERATURE" -> "la température";
            case "POULS" -> "le pouls";
            case "FREQUENCE_CARDIAQUE" -> "la fréquence cardiaque";
            case "SATURATION" -> "la saturation en oxygène";
            case "TENSION_ARTERIELLE" -> "la tension artérielle";
            case "GLYCEMIE" -> "la glycémie";
            default -> type.toLowerCase().replace("_", " ");
        };
    }
}