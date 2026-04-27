package com.example.dossiemedicale.services;

import com.example.dossiemedicale.DTO.*;
import com.example.dossiemedicale.entities.*;
import com.example.dossiemedicale.repositoories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DossierResumeService {

    private final DossierMedicalRepository dossierMedicalRepository;
    private final ConstanteVitaleRepository constanteVitaleRepository;
    private final AlerteRepository alerteRepository;
    private final ExamenRepository examenRepository;
    private final ImagerieMedicaleRepository imagerieMedicaleRepository;
    private final HospitalisationRepository hospitalisationRepository;

    public DossierResumeResponse getResumeComplet(Long dossierId) {

        DossierMedical dossier = dossierMedicalRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier médical introuvable"));

        Enfant enfant = dossier.getEnfant();
        Patient patient = enfant.getPatient();

        List<ConstanteVitale> constantes = constanteVitaleRepository
                .findTop5ByDossier_IdDossierOrderByDateMesureDesc(dossierId);

        List<Alerte> alertes = alerteRepository
                .findTop5ByConstante_Dossier_IdDossierOrderByDateDeclenchementDesc(dossierId);

        List<Examen> examens = examenRepository
                .findTop5ByDossier_IdDossierOrderByDateExamenDesc(dossierId);

        List<ImagerieMedicale> imageries = imagerieMedicaleRepository
                .findTop5ByDossier_IdDossierOrderByDateExamenDesc(dossierId);

        List<Hospitalisation> hospitalisations = hospitalisationRepository
                .findTop5ByEnfant_IdEnfantOrderByDateEntreeDesc(enfant.getIdEnfant());

        List<ConstanteResumeItem> constantesDto = constantes.stream()
                .map(c -> ConstanteResumeItem.builder()
                        .idConstante(c.getIdConstante())
                        .type(c.getType())
                        .valeur(c.getValeur())
                        .seuilMin(c.getSeuilMin())
                        .seuilMax(c.getSeuilMax())
                        .dateMesure(c.getDateMesure())
                        .statut(calculerStatut(c))
                        .build())
                .collect(Collectors.toList());

        List<AlerteResumeItem> alertesDto = alertes.stream()
                .map(a -> AlerteResumeItem.builder()
                        .idAlerte(a.getIdAlerte())
                        .niveau(a.getNiveau())
                        .message(a.getMessage())
                        .dateDeclenchement(a.getDateDeclenchement())
                        .typeConstante(a.getConstante() != null ? a.getConstante().getType() : null)
                        .build())
                .collect(Collectors.toList());

        List<ExamenResumeItem> examensDto = examens.stream()
                .map(e -> ExamenResumeItem.builder()
                        .idExamen(e.getIdExamen())
                        .type(e.getType())
                        .resultat(e.getResultat())
                        .dateExamen(e.getDateExamen())
                        .build())
                .collect(Collectors.toList());

        List<ImagerieResumeItem> imageriesDto = imageries.stream()
                .map(i -> ImagerieResumeItem.builder()
                        .idImagerie(i.getIdImagerie())
                        .type(i.getType())
                        .description(i.getDescription())
                        .dateExamen(i.getDateExamen())
                        .cheminFichier(i.getCheminFichier())
                        .build())
                .collect(Collectors.toList());

        List<HospitalisationResumeItem> hospitalisationsDto = hospitalisations.stream()
                .map(h -> HospitalisationResumeItem.builder()
                        .idHospitalisation(h.getIdHospitalisation())
                        .dateEntree(h.getDateEntree())
                        .dateSortie(h.getDateSortie())
                        .motif(h.getMotif())
                        .serviceHospitalier(h.getServiceHospitalier())
                        .build())
                .collect(Collectors.toList());

        String resumeGlobal = genererResumeTexte(
                enfant,
                patient,
                constantesDto,
                alertesDto,
                examensDto,
                imageriesDto,
                hospitalisationsDto
        );

        return DossierResumeResponse.builder()
                .idDossier(dossier.getIdDossier())
                .code(dossier.getCode())
                .dateCreation(dossier.getDateCreation())

                .idEnfant(enfant.getIdEnfant())
                .nomEnfant(enfant.getNom())
                .prenomEnfant(enfant.getPrenom())
                .ageEnfant(enfant.getAge())
                .sexeEnfant(enfant.getSexe())
                .tailleEnfant(enfant.getTaille())
                .poidsEnfant(enfant.getPoids())

                .idPatient(patient.getIdPatient())
                .nomPatient(patient.getNom())
                .prenomPatient(patient.getPrenom())
                .emailPatient(patient.getEmail())

                .dernieresConstantes(constantesDto)
                .dernieresAlertes(alertesDto)
                .derniersExamens(examensDto)
                .dernieresImageries(imageriesDto)
                .dernieresHospitalisations(hospitalisationsDto)

                .resumeGlobal(resumeGlobal)
                .build();
    }

    private String calculerStatut(ConstanteVitale c) {
        if (c.getValeur() == null) return "INCONNU";
        if (c.getSeuilMin() != null && c.getValeur() < c.getSeuilMin()) return "BAS";
        if (c.getSeuilMax() != null && c.getValeur() > c.getSeuilMax()) return "HAUT";
        return "NORMAL";
    }

    private String genererResumeTexte(
            Enfant enfant,
            Patient patient,
            List<ConstanteResumeItem> constantes,
            List<AlerteResumeItem> alertes,
            List<ExamenResumeItem> examens,
            List<ImagerieResumeItem> imageries,
            List<HospitalisationResumeItem> hospitalisations
    ) {
        StringBuilder sb = new StringBuilder();

        String prenomEnfant = formaterNom(enfant.getPrenom());
        String nomEnfant = formaterNom(enfant.getNom());

        sb.append("Synthèse clinique du dossier médical de ")
                .append(prenomEnfant);

        if (!nomEnfant.isBlank()) {
            sb.append(" ").append(nomEnfant);
        }

        if (enfant.getAge() != null) {
            sb.append(", ");
            if ("F".equalsIgnoreCase(enfant.getSexe())) {
                sb.append("âgée de ");
            } else {
                sb.append("âgé de ");
            }
            sb.append(enfant.getAge()).append(" ans");
        }
        sb.append(". ");

        sb.append("Le dossier regroupe les éléments de suivi clinique de l’enfant, ");
        sb.append("incluant les constantes vitales, les alertes de surveillance, ");
        sb.append("les examens biologiques, les examens d’imagerie ainsi que les antécédents d’hospitalisation. ");

        if (!constantes.isEmpty()) {
            ConstanteResumeItem c = constantes.get(0);

            sb.append("Les données de surveillance mettent notamment en évidence ");

            String typeConstante = normaliserType(c.getType());

            if (typeConstante.startsWith("la ") || typeConstante.startsWith("le ")
                    || typeConstante.startsWith("l’") || typeConstante.startsWith("l'")) {
                sb.append(typeConstante);
            } else {
                sb.append(typeConstante);
            }

            if (c.getValeur() != null) {
                sb.append(" mesurée à ").append(c.getValeur());
            }

            if ("TEMPERATURE".equalsIgnoreCase(c.getType())) {
                sb.append(" °C");
            } else if ("POULS".equalsIgnoreCase(c.getType()) || "FREQUENCE_CARDIAQUE".equalsIgnoreCase(c.getType())) {
                sb.append(" bpm");
            }

            if ("BAS".equalsIgnoreCase(c.getStatut())) {
                sb.append(", en dessous de l’intervalle de référence attendu");
            } else if ("HAUT".equalsIgnoreCase(c.getStatut())) {
                sb.append(", au-dessus de l’intervalle de référence attendu");
            } else if ("NORMAL".equalsIgnoreCase(c.getStatut())) {
                sb.append(", dans l’intervalle de référence");
            }

            sb.append(". ");
        }

        if (!alertes.isEmpty()) {
            sb.append("Des alertes de surveillance ont été enregistrées");

            String typeConstanteAlerte = alertes.get(0).getTypeConstante();
            if (typeConstanteAlerte != null && !typeConstanteAlerte.isBlank()) {
                sb.append(", en rapport notamment avec ")
                        .append(normaliserType(typeConstanteAlerte));
            }

            sb.append(". ");
        }

        if (!examens.isEmpty()) {
            sb.append("Les examens disponibles comportent notamment ");
            for (int i = 0; i < examens.size(); i++) {
                sb.append(examens.get(i).getType());
                if (i < examens.size() - 2) {
                    sb.append(", ");
                } else if (i == examens.size() - 2) {
                    sb.append(" et ");
                }
            }
            sb.append(". ");
        }

        if (!imageries.isEmpty()) {
            sb.append("Le dossier comprend également des examens d’imagerie, incluant ");
            for (int i = 0; i < imageries.size(); i++) {
                String type = imageries.get(i).getType() != null ? imageries.get(i).getType().toLowerCase() : "imagerie";
                sb.append(type);
                if (i < imageries.size() - 2) {
                    sb.append(", ");
                } else if (i == imageries.size() - 2) {
                    sb.append(" et ");
                }
            }
            sb.append(". ");
        }

        if (!hospitalisations.isEmpty()) {
            HospitalisationResumeItem h = hospitalisations.get(0);

            sb.append("Un antécédent d’hospitalisation est retrouvé");

            if (h.getServiceHospitalier() != null && !h.getServiceHospitalier().isBlank()) {
                sb.append(" dans le service ").append(h.getServiceHospitalier());
            }

            if (h.getMotif() != null && !h.getMotif().isBlank()) {
                sb.append(" pour le motif suivant : ").append(h.getMotif());
            }

            sb.append(". ");
        }

        String prenomPatient = formaterNom(patient.getPrenom());
        String nomPatient = formaterNom(patient.getNom());

        if (!prenomPatient.isBlank() || !nomPatient.isBlank()) {
            sb.append("Le responsable légal enregistré est ");
            if (!prenomPatient.isBlank()) {
                sb.append(prenomPatient);
                if (!nomPatient.isBlank()) {
                    sb.append(" ");
                }
            }
            if (!nomPatient.isBlank()) {
                sb.append(nomPatient);
            }
            sb.append(". ");
        }

        sb.append("L’ensemble de ces éléments doit être interprété en corrélation avec le contexte clinique, ");
        sb.append("l’évolution de l’état de l’enfant et l’appréciation médicale spécialisée.");

        return sb.toString();
    }

    private String formaterNom(String texte) {
        if (texte == null || texte.isBlank()) {
            return "";
        }

        String[] mots = texte.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String mot : mots) {
            if (mot.isBlank()) {
                continue;
            }
            sb.append(Character.toUpperCase(mot.charAt(0)))
                    .append(mot.substring(1))
                    .append(" ");
        }

        return sb.toString().trim();
    }

    private String normaliserType(String type) {
        if (type == null || type.isBlank()) {
            return "une constante vitale";
        }

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