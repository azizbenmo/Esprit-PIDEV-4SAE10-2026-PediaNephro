package com.example.consultation_microservice.controllers;

import com.example.consultation_microservice.entities.*;
import com.example.consultation_microservice.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/apiConsultation/avis")
public class AvisController {

    @Autowired private AvisRepository avisRepository;
    @Autowired private MedecinRepository medecinRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private ConsultationRepository consultationRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> soumettreAvis(@RequestBody Map<String, Object> body) {
        if (body == null || body.get("medecinId") == null || body.get("patientId") == null || body.get("note") == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "medecinId, patientId et note sont obligatoires."));
        }

        final Long medecinId;
        final Long patientRef;
        final Integer note;
        try {
            medecinId = Long.valueOf(body.get("medecinId").toString());
            patientRef = Long.valueOf(body.get("patientId").toString());
            note = Integer.valueOf(body.get("note").toString());
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "medecinId, patientId ou note invalide."));
        }

        if (note < 1 || note > 5) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La note doit être comprise entre 1 et 5."));
        }

        if (body.containsKey("consultationId") && body.get("consultationId") != null) {
            Long consultationId = Long.valueOf(body.get("consultationId").toString());
            if (avisRepository.findByConsultationId(consultationId).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Un avis a déjà été soumis pour cette consultation."));
            }
        }

        Medecin medecin = medecinRepository.findById(medecinId)
                .orElse(null);
        if (medecin == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Médecin non trouvé pour la réservation d'avis."));
        }

        // patientId côté front = souvent l'id User (JWT) : créer la fiche consultation si absente (comme demande de consultation).
        Patient patient = resolvePatientForAvis(body, patientRef);

        Avis avis = new Avis();
        avis.setMedecin(medecin);
        avis.setPatient(patient);
        avis.setNote(note);
        avis.setDateAvis(LocalDateTime.now());

        if (body.containsKey("commentaire") && body.get("commentaire") != null) {
            avis.setCommentaire(body.get("commentaire").toString());
        }

        if (body.containsKey("consultationId") && body.get("consultationId") != null) {
            Long consultationId = Long.valueOf(body.get("consultationId").toString());
            consultationRepository.findById(consultationId).ifPresent(avis::setConsultation);
        }

        avisRepository.save(avis);

        // Renvoyer la moyenne mise à jour directement dans la réponse
        Double moyenne = avisRepository.findAverageNoteByMedecinId(medecinId);
        long total = avisRepository.countByMedecinId(medecinId);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Avis soumis avec succès.");
        result.put("moyenneNote", moyenne != null ? Math.round(moyenne * 10.0) / 10.0 : null);
        result.put("nombreAvis", total);
        result.put("medecinId", medecinId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/medecin/{medecinId}/rating")
    public Map<String, Object> getRating(@PathVariable Long medecinId) {
        Double moyenne = avisRepository.findAverageNoteByMedecinId(medecinId);
        long total = avisRepository.countByMedecinId(medecinId);
        Map<String, Object> result = new HashMap<>();
        result.put("medecinId", medecinId);
        result.put("moyenneNote", moyenne != null ? Math.round(moyenne * 10.0) / 10.0 : null);
        result.put("nombreAvis", total);
        return result;
    }

    @GetMapping("/ratings")
    public List<Map<String, Object>> getAllRatings() {
        return avisRepository.findDistinctMedecinIds().stream()
                .map(medecinId -> {
                    Double moyenne = avisRepository.findAverageNoteByMedecinId(medecinId);
                    long total = avisRepository.countByMedecinId(medecinId);
                    Map<String, Object> r = new HashMap<>();
                    r.put("medecinId", medecinId);
                    r.put("moyenneNote", moyenne != null ? Math.round(moyenne * 10.0) / 10.0 : null);
                    r.put("nombreAvis", total);
                    return r;
                })
                .toList();
    }

    @GetMapping("/consultation/{consultationId}/existe")
    public Map<String, Boolean> avisExiste(@PathVariable Long consultationId) {
        boolean existe = avisRepository.findByConsultationId(consultationId).isPresent();
        return Map.of("existe", existe);
    }

    private Patient resolvePatientForAvis(Map<String, Object> body, Long ref) {
        Patient patient = patientRepository.findByUserId(ref)
                .orElseGet(() -> createPatientStubFromAvisRequest(body, ref));
        return syncPatientIdentityFromAvisRequest(patient, body, ref);
    }

    private Patient createPatientStubFromAvisRequest(Map<String, Object> body, Long userRef) {
        Patient p = new Patient();
        p.setUserId(userRef);
        p.setPrenom(readOptionalString(body, "patientPrenom", "Patient"));
        p.setNom(readOptionalString(body, "patientNom", "—"));
        p.setEmail(readOptionalString(body, "patientEmail", "patient-" + userRef + "@pedianephro.local"));
        return patientRepository.save(p);
    }

    private Patient syncPatientIdentityFromAvisRequest(Patient existing, Map<String, Object> body, Long userRef) {
        boolean changed = false;

        if (existing.getUserId() == null || !existing.getUserId().equals(userRef)) {
            existing.setUserId(userRef);
            changed = true;
        }

        String prenom = readOptionalString(body, "patientPrenom", "").trim();
        if (!prenom.isEmpty() && !prenom.equals(existing.getPrenom())) {
            existing.setPrenom(prenom);
            changed = true;
        }

        String nom = readOptionalString(body, "patientNom", "").trim();
        if (!nom.isEmpty() && !nom.equals(existing.getNom())) {
            existing.setNom(nom);
            changed = true;
        }

        String email = readOptionalString(body, "patientEmail", "").trim();
        if (!email.isEmpty() && !email.equals(existing.getEmail())) {
            existing.setEmail(email);
            changed = true;
        }

        return changed ? patientRepository.save(existing) : existing;
    }

    private static String readOptionalString(Map<String, Object> body, String key, String defaultValue) {
        if (!body.containsKey(key) || body.get(key) == null) {
            return defaultValue;
        }
        String s = body.get(key).toString().trim();
        return s.isEmpty() ? defaultValue : s;
    }
}
