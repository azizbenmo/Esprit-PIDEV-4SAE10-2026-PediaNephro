package com.example.consultation_microservice.services;


import com.example.consultation_microservice.entities.Consultation;
import com.example.consultation_microservice.entities.RapportConsultation;
import com.example.consultation_microservice.repositories.ConsultationRepository;
import com.example.consultation_microservice.repositories.RapportConsultationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResumeDossierService {

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private RapportConsultationRepository rapportConsultationRepository;

    @Autowired
    private OllamaService ollamaService;

    public String genererResumePourConsultation(Long consultationId) {
        // Récupérer la consultation en cours
        Consultation consultationEnCours = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));

        Long patientId = consultationEnCours.getPatient().getId();
        String nomPatient = consultationEnCours.getPatient().getPrenom()
                + " " + consultationEnCours.getPatient().getNom();

        // Récupérer tout l'historique du patient (sauf la consultation en cours)
        List<Consultation> historique = consultationRepository.findByPatientId(patientId)
                .stream()
                .filter(c -> !c.getId().equals(consultationId))
                .filter(c -> c.getStatut().name().equals("TERMINEE"))
                .sorted((a, b) -> a.getDateSouhaitee().compareTo(b.getDateSouhaitee()))
                .toList();

        if (historique.isEmpty()) {
            return "Aucun antécédent médical enregistré pour ce patient. "
                    + "Il s'agit de sa première consultation.";
        }

        // Construire le contexte pour le LLM
        StringBuilder contexte = new StringBuilder();
        contexte.append("Voici l'historique médical du patient ").append(nomPatient).append(" :\n\n");

        for (Consultation c : historique) {
            contexte.append("--- Consultation du ")
                    .append(c.getDateSouhaitee().toLocalDate()).append(" ---\n");
            contexte.append("Spécialité : ").append(c.getSpecialite()).append("\n");
            contexte.append("Motif : ").append(c.getMotif()).append("\n");

            if (c.getSymptomes() != null && !c.getSymptomes().isBlank()) {
                contexte.append("Symptômes : ").append(c.getSymptomes()).append("\n");
            }
            if (c.getAntecedents() != null && !c.getAntecedents().isBlank()) {
                contexte.append("Antécédents : ").append(c.getAntecedents()).append("\n");
            }
            if (c.getResultatsBio() != null && !c.getResultatsBio().isBlank()) {
                contexte.append("Résultats biologiques : ").append(c.getResultatsBio()).append("\n");
            }

            // Ajouter le rapport si disponible
            Optional<RapportConsultation> rapport =
                    rapportConsultationRepository.findByConsultationId(c.getId());
            if (rapport.isPresent() && rapport.get().isEstSoumis()) {
                RapportConsultation r = rapport.get();
                if (r.getDiagnostic() != null && !r.getDiagnostic().isBlank())
                    contexte.append("Diagnostic : ").append(r.getDiagnostic()).append("\n");
                if (r.getObservations() != null && !r.getObservations().isBlank())
                    contexte.append("Observations : ").append(r.getObservations()).append("\n");
                if (r.getRecommandations() != null && !r.getRecommandations().isBlank())
                    contexte.append("Recommandations : ").append(r.getRecommandations()).append("\n");
                if (r.getEtatPatient() != null && !r.getEtatPatient().isBlank())
                    contexte.append("État du patient : ").append(r.getEtatPatient()).append("\n");
            }
            contexte.append("\n");
        }

        // Consultation actuelle
        contexte.append("--- Consultation actuelle ---\n");
        contexte.append("Spécialité : ").append(consultationEnCours.getSpecialite()).append("\n");
        contexte.append("Motif : ").append(consultationEnCours.getMotif()).append("\n");
        if (consultationEnCours.getSymptomes() != null)
            contexte.append("Symptômes déclarés : ").append(consultationEnCours.getSymptomes()).append("\n");

        // Prompt final
        String prompt = """
            Tu es un assistant médical. En te basant sur l'historique ci-dessous, 
            génère un résumé narratif concis (10-15 lignes maximum) destiné au médecin 
            qui va recevoir ce patient en consultation. 
            
            Le résumé doit inclure :
            - L'évolution générale de l'état de santé du patient
            - Les pathologies et antécédents importants
            - Les traitements et recommandations passés
            - Les points d'attention pour la consultation actuelle
            
            Réponds en français, de façon professionnelle et structurée.
            
            """ + contexte;

        return ollamaService.genererResume(prompt);
    }
}