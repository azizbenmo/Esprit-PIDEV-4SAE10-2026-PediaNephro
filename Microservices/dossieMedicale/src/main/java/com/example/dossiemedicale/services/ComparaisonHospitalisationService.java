package com.example.dossiemedicale.services;

import com.example.dossiemedicale.DTO.ComparaisonHospitalisationResponse;
import com.example.dossiemedicale.DTO.PeriodeStats;
import com.example.dossiemedicale.entities.Alerte;
import com.example.dossiemedicale.entities.ConstanteVitale;
import com.example.dossiemedicale.entities.Hospitalisation;
import com.example.dossiemedicale.repositoories.AlerteRepository;
import com.example.dossiemedicale.repositoories.ConstanteVitaleRepository;
import com.example.dossiemedicale.repositoories.HospitalisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComparaisonHospitalisationService {

    private final HospitalisationRepository hospitalisationRepository;
    private final ConstanteVitaleRepository constanteRepository;
    private final AlerteRepository alerteRepository;

    public ComparaisonHospitalisationResponse comparer(Long idHospitalisation) {

        Hospitalisation hosp = hospitalisationRepository.findById(idHospitalisation)
                .orElseThrow(() -> new RuntimeException("Hospitalisation introuvable"));

        Date dateEntree = hosp.getDateEntree();
        Date dateSortie = hosp.getDateSortie();
        Long enfantId = hosp.getEnfant().getIdEnfant();
        Long dossierId = hosp.getEnfant().getDossierMedical().getIdDossier();

        // AVANT hospitalisation
        List<ConstanteVitale> avant = constanteRepository
                .findByDossier_Enfant_IdEnfantAndDateMesureBefore(enfantId, dateEntree);

        List<Alerte> alertesAvant = alerteRepository
                .findByConstante_Dossier_IdDossierAndDateDeclenchementBefore(dossierId, dateEntree);

        // APRES hospitalisation
        List<ConstanteVitale> apres = constanteRepository
                .findByDossier_Enfant_IdEnfantAndDateMesureAfter(enfantId, dateSortie);

        List<Alerte> alertesApres = alerteRepository
                .findByConstante_Dossier_IdDossierAndDateDeclenchementAfter(dossierId, dateSortie);

        PeriodeStats statsAvant = calculerStats(avant, alertesAvant);
        PeriodeStats statsApres = calculerStats(apres, alertesApres);

        String evolution = evaluerEvolution(statsAvant, statsApres);
        String interpretation = genererInterpretation(evolution, statsAvant, statsApres);

        return ComparaisonHospitalisationResponse.builder()
                .idHospitalisation(idHospitalisation)
                .periodeAvant(statsAvant)
                .periodeApres(statsApres)
                .evolution(evolution)
                .interpretation(interpretation)
                .build();
    }

    private PeriodeStats calculerStats(List<ConstanteVitale> constantes, List<Alerte> alertes) {
        double tempTotal = 0.0;
        double poulsTotal = 0.0;
        int tempCount = 0;
        int poulsCount = 0;

        if (constantes != null) {
            for (ConstanteVitale c : constantes) {
                if (c == null || c.getType() == null || c.getValeur() == null) {
                    continue;
                }

                if ("TEMPERATURE".equalsIgnoreCase(c.getType())) {
                    tempTotal += c.getValeur();
                    tempCount++;
                }

                if ("POULS".equalsIgnoreCase(c.getType())) {
                    poulsTotal += c.getValeur();
                    poulsCount++;
                }
            }
        }

        return PeriodeStats.builder()
                .temperatureMoyenne(tempCount > 0 ? tempTotal / tempCount : null)
                .poulsMoyen(poulsCount > 0 ? poulsTotal / poulsCount : null)
                .nbAlertes(alertes != null ? alertes.size() : 0)
                .build();
    }

    private String evaluerEvolution(PeriodeStats avant, PeriodeStats apres) {
        int score = 0;

        // Alertes : moins d'alertes après = amélioration
        if (apres.getNbAlertes() < avant.getNbAlertes()) {
            score++;
        } else if (apres.getNbAlertes() > avant.getNbAlertes()) {
            score--;
        }

        // Température : plus proche de 37°C = mieux
        if (avant.getTemperatureMoyenne() != null && apres.getTemperatureMoyenne() != null) {
            double diffAvant = Math.abs(avant.getTemperatureMoyenne() - 37.0);
            double diffApres = Math.abs(apres.getTemperatureMoyenne() - 37.0);

            if (diffApres < diffAvant) {
                score++;
            } else if (diffApres > diffAvant) {
                score--;
            }
        }

        // Pouls : retour dans l’intervalle 60-100 = mieux
        if (avant.getPoulsMoyen() != null && apres.getPoulsMoyen() != null) {
            boolean avantNormal = avant.getPoulsMoyen() >= 60 && avant.getPoulsMoyen() <= 100;
            boolean apresNormal = apres.getPoulsMoyen() >= 60 && apres.getPoulsMoyen() <= 100;

            if (!avantNormal && apresNormal) {
                score++;
            } else if (avantNormal && !apresNormal) {
                score--;
            }
        }

        if (score >= 2) {
            return "AMELIORATION";
        }
        if (score <= -2) {
            return "AGGRAVATION";
        }
        return "STABLE";
    }

    private String genererInterpretation(String evolution, PeriodeStats avant, PeriodeStats apres) {
        return switch (evolution) {
            case "AMELIORATION" ->
                    "Une amélioration clinique est observée après la période d’hospitalisation, marquée par une stabilisation des paramètres vitaux et une diminution des événements d’alerte.";
            case "AGGRAVATION" ->
                    "L’évolution clinique après hospitalisation suggère une aggravation, avec persistance ou augmentation des anomalies, nécessitant une surveillance médicale rapprochée.";
            default ->
                    "L’état clinique du patient demeure globalement stable, sans variation significative des constantes vitales ni des événements d’alerte.";
        };
    }
}