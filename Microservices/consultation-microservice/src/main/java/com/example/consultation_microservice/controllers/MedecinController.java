package com.example.consultation_microservice.controllers;

import com.example.consultation_microservice.entities.*;
import com.example.consultation_microservice.repositories.ConsultationRepository;
import com.example.consultation_microservice.repositories.MedecinRepository;
import com.example.consultation_microservice.repositories.PatientRepository;
import com.example.consultation_microservice.repositories.RapportConsultationRepository;
import com.example.consultation_microservice.services.DossierMedicalService;
import com.example.consultation_microservice.services.DossierMedicalSyncService;
import com.example.consultation_microservice.services.EmailService;
import com.example.consultation_microservice.services.ResumeDossierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/apiConsultation/medecin")
@Slf4j
public class MedecinController {

    @Autowired private ConsultationRepository consultationRepository;
    @Autowired private RapportConsultationRepository rapportConsultationRepository;
    @Autowired private EmailService emailService;
    @Autowired private PatientRepository patientRepository;
    @Autowired private MedecinRepository medecinRepository;
    @Autowired private DossierMedicalService dossierMedicalService;
    @Autowired private DossierMedicalSyncService dossierMedicalSyncService;
    @Autowired private ResumeDossierService resumeDossierService;

    @GetMapping("/{id}/profil")
    public Medecin getProfilMedecin(@PathVariable Long id) {
        return medecinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));
    }

    @GetMapping("/{medecinId}/demandes")
    public List<Consultation> getDemandes(@PathVariable Long medecinId) {
        return consultationRepository.findByMedecinIdAndStatut(medecinId, ConsultationStatus.DEMANDEE);
    }

    /**
     * Demandes du médecin identifié par son userId (id compte User/Doctor).
     */
    @GetMapping("/user/{userId}/demandes")
    public List<Consultation> getDemandesByUserId(@PathVariable Long userId) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable pour userId=" + userId));
        return consultationRepository.findByMedecinIdAndStatut(medecin.getId(), ConsultationStatus.DEMANDEE);
    }

    /**
     * Consultations acceptées du médecin identifié par son userId (compte User/Doctor).
     */
    @GetMapping("/user/{userId}/consultations-acceptees")
    public List<Consultation> getConsultationsAccepteesByUserId(@PathVariable Long userId) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable pour userId=" + userId));
        return consultationRepository.findByMedecinIdAndStatut(medecin.getId(), ConsultationStatus.ACCEPTEE);
    }

    /**
     * Toutes les consultations du médecin identifié par son userId (compte User/Doctor).
     */
    @GetMapping("/user/{userId}/consultations")
    public List<Consultation> getConsultationsByUserId(@PathVariable Long userId) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable pour userId=" + userId));
        return consultationRepository.findByMedecinId(medecin.getId());
    }

    /**
     * Patients suivis par le mÃ©decin connectÃ©, reconstruits depuis ses consultations.
     * Fallback utile quand la relation doctor->patients du microservice User n'est pas encore synchronisÃ©e.
     */
    @GetMapping("/user/{userId}/patients")
    public List<Map<String, Object>> getPatientsByUserId(@PathVariable Long userId) {
        List<Consultation> consultations = new ArrayList<>();

        consultations.addAll(consultationRepository.findByMedecinUserId(userId));

        medecinRepository.findByUserId(userId)
                .ifPresent(m -> consultations.addAll(consultationRepository.findByMedecinId(m.getId())));

        medecinRepository.findById(userId)
                .ifPresent(m -> consultations.addAll(consultationRepository.findByMedecinId(m.getId())));

        Map<String, Map<String, Object>> byPatient = new LinkedHashMap<>();

        for (Consultation c : consultations) {
            if (c == null || c.getPatient() == null) {
                continue;
            }
            Patient p = c.getPatient();
            Long ref = p.getUserId() != null ? p.getUserId() : p.getId();
            String key = String.valueOf(ref != null ? ref : p.getId());

            Map<String, Object> row = byPatient.computeIfAbsent(key, k -> {
                Map<String, Object> r = new LinkedHashMap<>();
                String fullName = ((p.getPrenom() != null ? p.getPrenom() : "") + " "
                        + (p.getNom() != null ? p.getNom() : "")).trim();
                r.put("id", ref);
                r.put("fullName", fullName);
                r.put("full_name", fullName);
                r.put("email", p.getEmail());
                r.put("parentName", null);
                r.put("parent_name", null);
                return r;
            });

            String parentFullName = ((c.getDemandeurParentPrenom() != null ? c.getDemandeurParentPrenom() : "") + " "
                    + (c.getDemandeurParentNom() != null ? c.getDemandeurParentNom() : "")).trim();
            if (!parentFullName.isBlank() && row.get("parentName") == null) {
                row.put("parentName", parentFullName);
                row.put("parent_name", parentFullName);
            }
        }

        return new ArrayList<>(byPatient.values());
    }

    @PostMapping("/{id}/decision")
    public Consultation decisionConsultation(@PathVariable Long id, @RequestParam String decision) {
        Consultation c = consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));

        if (decision.equalsIgnoreCase("ACCEPTER")) {
            c.setStatut(ConsultationStatus.ACCEPTEE);
            c.setDateConfirmee(LocalDateTime.now());
        } else if (decision.equalsIgnoreCase("REFUSER")) {
            c.setStatut(ConsultationStatus.REFUSEE);
        } else {
            throw new RuntimeException("Decision invalide");
        }

        Consultation saved = consultationRepository.save(c);
        Patient patientComplet = patientRepository.findById(saved.getPatient().getId())
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));

        Set<String> recipients = collectNotificationEmails(saved, patientComplet);
        if (decision.equalsIgnoreCase("ACCEPTER")) {
            for (String recipient : recipients) {
                emailService.sendAcceptation(recipient, saved);
            }
        } else {
            for (String recipient : recipients) {
                emailService.sendRefus(recipient, saved);
            }
        }
        return saved;
    }

    private Set<String> collectNotificationEmails(Consultation consultation, Patient patientComplet) {
        Set<String> recipients = new LinkedHashSet<>();
        if (consultation != null && consultation.getDemandeurParentEmail() != null) {
            String parentEmail = consultation.getDemandeurParentEmail().trim();
            if (!parentEmail.isBlank()) {
                recipients.add(parentEmail);
            }
        }
        if (patientComplet != null && patientComplet.getEmail() != null) {
            String patientEmail = patientComplet.getEmail().trim();
            if (!patientEmail.isBlank()) {
                recipients.add(patientEmail);
            }
        }
        return recipients;
    }

    /**
     * GET /{id}/rapport
     * ✅ Retourne le rapport existant, OU crée automatiquement un rapport vide en BD.
     * Appelé dès que le médecin clique sur "Rédiger le rapport".
     */
    @GetMapping("/{id}/rapport")
    public ResponseEntity<RapportConsultation> getRapport(@PathVariable Long id) {
        Consultation c = consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));

        RapportConsultation rapport = rapportConsultationRepository
                .findByConsultationId(id)
                .orElseGet(() -> {
                    // ✅ Création immédiate d'un rapport vide en BD
                    RapportConsultation nouveau = new RapportConsultation();
                    nouveau.setConsultation(c);
                    nouveau.setCreatedAt(LocalDateTime.now());
                    nouveau.setDiagnostic("");
                    nouveau.setObservations("");
                    nouveau.setRecommandations("");
                    nouveau.setEtatPatient("");
                    nouveau.setEstSoumis(false);
                    return rapportConsultationRepository.save(nouveau);
                });

        return ResponseEntity.ok(rapport);
    }

    /**
     * PUT /{id}/rapport
     * ✅ Met à jour le rapport (déjà créé) avec les données du formulaire.
     * Marque la consultation comme TERMINEE et lie au dossier médical.
     */
    @PutMapping("/{id}/rapport")
    public ResponseEntity<RapportConsultation> soumettreRapport(
            @PathVariable Long id,
            @RequestParam String diagnostic,
            @RequestParam String observations,
            @RequestParam String recommandations,
            @RequestParam String etatPatient,
            @RequestParam(required = false) MultipartFile fichier) {

        Consultation c = consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));

        RapportConsultation rapport = rapportConsultationRepository
                .findByConsultationId(id)
                .orElseThrow(() -> new RuntimeException("Rapport introuvable"));

        // Mise à jour des champs
        rapport.setDiagnostic(diagnostic);
        rapport.setObservations(observations);
        rapport.setRecommandations(recommandations);
        rapport.setEtatPatient(etatPatient);
        rapport.setEstSoumis(true);

        // Fichier joint
        /*if (fichier != null && !fichier.isEmpty()) {
            File uploadDir = new File("uploads");
            if (!uploadDir.exists()) uploadDir.mkdirs();
            String chemin = uploadDir.getAbsolutePath() + File.separator + fichier.getOriginalFilename();
            fichier.transferTo(new File(chemin));
            rapport.setFichierPath(chemin);
        }*/
        if (fichier != null && !fichier.isEmpty()) {
            try {
                File uploadDir = new File("uploads");
                if (!uploadDir.exists()) uploadDir.mkdirs();
                String nomFichier = fichier.getOriginalFilename();
                String chemin = uploadDir.getAbsolutePath() + File.separator + nomFichier;
                fichier.transferTo(new File(chemin));
                // ✅ Stocker uniquement le nom du fichier (pas le chemin absolu)
                rapport.setFichierPath(nomFichier);
            } catch (Exception ex) {
                // Ne pas bloquer la soumission du rapport si l'upload échoue.
                log.warn("Upload du fichier rapport échoué (consultationId={}): {}", id, ex.getMessage());
            }
        }

        RapportConsultation saved = rapportConsultationRepository.save(rapport);

        // ✅ Consultation → TERMINEE
        c.setStatut(ConsultationStatus.TERMINEE);
        consultationRepository.save(c);

        try {
            // ✅ Lier au dossier médical (best effort)
            Long patientRef = c.getPatient().getUserId() != null
                    ? c.getPatient().getUserId()
                    : c.getPatient().getId();
            Long dossierMedicalId = dossierMedicalService.ajouterRapportAuDossier(patientRef, saved);
            dossierMedicalSyncService.syncRapportConsultation(c, saved, dossierMedicalId);
        } catch (Exception ex) {
            log.warn("Liaison/synchronisation du rapport vers dossieMedicale en echec (consultationId={}): {}",
                    c.getId(), ex.getMessage());
        }

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}/resume-dossier")
    public ResponseEntity<Map<String, String>> getResumeDossier(@PathVariable Long id) {
        String resume = resumeDossierService.genererResumePourConsultation(id);
        return ResponseEntity.ok(Map.of("resume", resume));
    }
}
