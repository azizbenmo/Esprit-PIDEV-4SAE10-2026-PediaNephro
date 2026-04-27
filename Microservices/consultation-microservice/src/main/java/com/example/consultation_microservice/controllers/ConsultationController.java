package com.example.consultation_microservice.controllers;

import com.example.consultation_microservice.entities.*;
import com.example.consultation_microservice.repositories.ConsultationRepository;
import com.example.consultation_microservice.repositories.MedecinRepository;
import com.example.consultation_microservice.repositories.PatientRepository;
import com.example.consultation_microservice.services.EmailService;
import com.example.consultation_microservice.services.TriageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/apiConsultation/consultation")
public class ConsultationController {

    @Autowired private ConsultationRepository consultationRepository;
    @Autowired private MedecinRepository medecinRepository;
    @Autowired private EmailService emailService;
    @Autowired private PatientRepository patientRepository;
    @Autowired private TriageService triageService;

    @GetMapping
    public List<Consultation> getAllConsultations() {
        return consultationRepository.findAll();
    }

    @GetMapping("/{id}")
    public Consultation getConsultationById(@PathVariable Long id) {
        return consultationRepository.findById(id).orElse(null);
    }

    /**
     * ✅ GET /creneaux-disponibles?medecinId=X&date=YYYY-MM-DD
     * Retourne la liste des heures disponibles pour un médecin à une date donnée.
     * Toutes les heures du planning sont retournées avec leur statut (libre/pris).
     */
    @GetMapping("/creneaux-disponibles")
    public ResponseEntity<List<Map<String, Object>>> getCreneauxDisponibles(
            @RequestParam Long medecinId,
            @RequestParam String date) {

        LocalDate localDate = LocalDate.parse(date);

        // Générer toutes les heures du planning selon le jour de semaine
        List<LocalTime> toutesLesHeures = new ArrayList<>();
        int dayOfWeek = localDate.getDayOfWeek().getValue(); // 1=Lun, 7=Dim

        if (dayOfWeek >= 1 && dayOfWeek <= 5) {
            // Lundi–Vendredi : 9h–15h
            for (int h = 9; h <= 15; h++) toutesLesHeures.add(LocalTime.of(h, 0));
        } else if (dayOfWeek == 6) {
            // Samedi : 9h–12h
            for (int h = 9; h <= 12; h++) toutesLesHeures.add(LocalTime.of(h, 0));
        }
        // Dimanche : aucun créneau

        // ✅ Récupérer les créneaux déjà pris — plage [00:00, lendemain 00:00)
        LocalDateTime debutJournee = localDate.atStartOfDay();
        LocalDateTime finJournee   = debutJournee.plusDays(1);
        List<LocalDateTime> prisList = consultationRepository
                .findCreneauxPrisByMedecinAndDate(medecinId, debutJournee, finJournee);

        Set<LocalTime> heuresPrises = prisList.stream()
                .map(LocalDateTime::toLocalTime)
                .collect(Collectors.toSet());

        // Construire la réponse
        List<Map<String, Object>> creneaux = toutesLesHeures.stream().map(heure -> {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("heure", String.format("%02d:00", heure.getHour()));
            c.put("disponible", !heuresPrises.contains(heure));
            return c;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(creneaux);
    }

    @PostMapping
    public ResponseEntity<?> createDemandeConsultation(@RequestBody Consultation consultation) {

        // Statut par défaut
        if (consultation.getStatut() == null) {
            consultation.setStatut(ConsultationStatus.DEMANDEE);
        }

        // Date souhaitée par défaut = maintenant
        if (consultation.getDateSouhaitee() == null) {
            consultation.setDateSouhaitee(LocalDateTime.now());
        }

        // Affectation automatique du médecin si ni id ni userId (profil User) fournis
        Medecin medecinStub = consultation.getMedecin();
        if (medecinStub == null
                || (medecinStub.getId() == null && medecinStub.getUserId() == null)) {
            Medecin medecin = medecinRepository
                    .findBySpecialiteAndDisponible(consultation.getSpecialite(), true)
                    .stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Aucun médecin disponible pour cette spécialité"));
            consultation.setMedecin(medecin);
        }

        // Patient local : id table consultation OU userId User MS ; sinon création automatique
        Patient patientComplet = resolvePatientForConsultation(consultation.getPatient());
        consultation.setPatient(patientComplet);

        // Résolution médecin : si le front envoie userId (= id doctor/User MS), il prime sur id (PK)
        // pour éviter un libellé annuaire correct mais une PK fiche consultation erronée (ex. Dupont).
        Medecin medecinComplet = resolveMedecinComplet(consultation.getMedecin());
        consultation.setMedecin(medecinComplet);

        // ✅ Vérification du conflit de créneau
        boolean creneauPris = consultationRepository.existsCreneauPris(
                medecinComplet.getId(),
                consultation.getDateSouhaitee()
        );

        if (creneauPris) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Ce créneau est déjà pris. Veuillez choisir une autre heure.");
            error.put("code", "CRENEAU_CONFLIT");
            return ResponseEntity.status(409).body(error);
        }

        // Calcul du triage
        consultation = triageService.calculerTriage(consultation);

        // Sauvegarde
        Consultation saved = consultationRepository.save(consultation);

        // Emails: notifier parent + patient (sans doublon) pour robustesse.
        for (String recipient : collectNotificationEmails(saved, patientComplet)) {
            emailService.sendConfirmation(recipient, saved);
        }
        if (saved.getNiveauUrgence() == NiveauUrgence.URGENTE) {
            emailService.sendAlerteUrgente(medecinComplet.getEmail(), saved);
        }

        return ResponseEntity.ok(saved);
    }

    /**
     * Résout la fiche {@link Patient} du microservice consultation : par clé locale, par {@code userId}
     * (identifiant utilisateur / patient côté User), ou création à la volée à partir du corps JSON.
     */
    private Patient resolvePatientForConsultation(Patient stub) {
        if (stub == null || (stub.getId() == null && stub.getUserId() == null)) {
            throw new RuntimeException("patient.id est obligatoire (identifiant utilisateur / patient User)");
        }
        Long userRef = stub.getUserId() != null ? stub.getUserId() : stub.getId();

        // Toujours prioriser le lien métier user_id pour éviter les collisions avec la PK locale.
        Patient patient = patientRepository.findByUserId(userRef)
                .orElseGet(() -> createPatientFromUserRef(stub, userRef));

        return syncPatientIdentity(patient, stub, userRef);
    }

    private Patient createPatientFromUserRef(Patient stub, Long userRef) {
        Patient p = new Patient();
        p.setUserId(userRef);
        String nom = stub.getNom() != null && !stub.getNom().isBlank() ? stub.getNom().trim() : "—";
        String prenom = stub.getPrenom() != null && !stub.getPrenom().isBlank() ? stub.getPrenom().trim() : "Patient";
        String email = stub.getEmail() != null && !stub.getEmail().isBlank()
                ? stub.getEmail().trim()
                : ("patient-" + userRef + "@pedianephro.local");
        p.setNom(nom);
        p.setPrenom(prenom);
        p.setEmail(email);
        return patientRepository.save(p);
    }

    private Patient syncPatientIdentity(Patient existing, Patient stub, Long userRef) {
        boolean changed = false;

        if (existing.getUserId() == null || !existing.getUserId().equals(userRef)) {
            existing.setUserId(userRef);
            changed = true;
        }

        String nom = (stub.getNom() == null || stub.getNom().isBlank()) ? null : stub.getNom().trim();
        if (nom != null && !nom.equals(existing.getNom())) {
            existing.setNom(nom);
            changed = true;
        }

        String prenom = (stub.getPrenom() == null || stub.getPrenom().isBlank()) ? null : stub.getPrenom().trim();
        if (prenom != null && !prenom.equals(existing.getPrenom())) {
            existing.setPrenom(prenom);
            changed = true;
        }

        String email = (stub.getEmail() == null || stub.getEmail().isBlank()) ? null : stub.getEmail().trim();
        if (email != null && !email.equals(existing.getEmail())) {
            existing.setEmail(email);
            changed = true;
        }

        return changed ? patientRepository.save(existing) : existing;
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
     * Priorité au {@code userId} (id utilisateur / doctor côté User MS) quand le front l’envoie,
     * pour coller au médecin choisi dans l’annuaire même si la PK {@code id} transité était fausse.
     */
    private Medecin resolveMedecinComplet(Medecin stub) {
        if (stub == null) {
            throw new RuntimeException("Médecin non renseigné");
        }
        if (stub.getUserId() != null) {
            return medecinRepository.findByUserId(stub.getUserId())
                    .orElseThrow(() -> new RuntimeException(
                            "Médecin non trouvé pour userId=" + stub.getUserId()
                                    + " (vérifiez medecin.user_id en base consultation)"));
        }
        if (stub.getId() != null) {
            return medecinRepository.findById(stub.getId())
                    .orElseThrow(() -> new RuntimeException("Médecin non trouvé pour id=" + stub.getId()));
        }
        throw new RuntimeException("Médecin non renseigné (id ou userId requis)");
    }

    @PostMapping("/{id}/annuler")
    public Consultation annulerConsultation(@PathVariable Long id) {
        Consultation c = consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));

        if (c.getStatut() == ConsultationStatus.TERMINEE) {
            throw new RuntimeException("Impossible d'annuler une consultation terminée");
        }

        c.setStatut(ConsultationStatus.ANNULEE);
        Consultation saved = consultationRepository.save(c);
        emailService.sendAnnulation(c.getPatient().getEmail(), saved);
        return saved;
    }

    @GetMapping("/medecins")
    public List<Medecin> getMedecinsBySpecialite(@RequestParam String specialite) {
        return medecinRepository.findBySpecialiteAndDisponible(specialite, true);
    }

    @GetMapping("/medecin/{medecinId}")
    public List<Consultation> getConsultationsByMedecin(@PathVariable Long medecinId) {
        return consultationRepository.findByMedecinId(medecinId);
    }

    @GetMapping("/medecins/charge")
    public List<Map<String, Object>> getChargeMedecins() {
        return medecinRepository.findAll().stream().map(medecin -> {
            long nbPatientsEnCours = consultationRepository
                    .countByMedecinIdAndStatutIn(medecin.getId(),
                            List.of(ConsultationStatus.DEMANDEE, ConsultationStatus.ACCEPTEE));
            Map<String, Object> result = new HashMap<>();
            result.put("medecinId", medecin.getId());
            result.put("nom", medecin.getNom());
            result.put("prenom", medecin.getPrenom());
            result.put("specialite", medecin.getSpecialite());
            result.put("disponible", medecin.getDisponible());
            result.put("patientsEnCours", nbPatientsEnCours);
            return result;
        }).collect(Collectors.toList());
    }

    @GetMapping("/{id}/triage")
    public Map<String, Object> getTriageResult(@PathVariable Long id) {
        Consultation c = consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));
        Map<String, Object> result = new HashMap<>();
        result.put("consultationId", c.getId());
        result.put("niveauUrgence", c.getNiveauUrgence());
        result.put("scoreUrgence", c.getScoreUrgence());
        result.put("justification", c.getJustificationTriage());
        result.put("patient", c.getPatient().getNom() + " " + c.getPatient().getPrenom());
        result.put("motif", c.getMotif());
        return result;
    }

    @GetMapping("/urgentes")
    public List<Consultation> getConsultationsUrgentes() {
        return consultationRepository.findByNiveauUrgenceOrderByScoreUrgenceDesc(NiveauUrgence.URGENTE);
    }
}
