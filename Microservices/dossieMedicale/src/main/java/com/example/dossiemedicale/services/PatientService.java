package com.example.dossiemedicale.services;

import com.example.dossiemedicale.entities.Patient;
import com.example.dossiemedicale.repositoories.PatientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    /**
     * Retrouve un patient dossier par email, ou le crée (lien implicite avec le compte parent / utilisateur).
     */
    @Transactional
    public Patient assurerPatientPourCompte(String email, String prenom, String nom, Long userId) {
        String normalized = email != null ? email.trim().toLowerCase() : "";
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("L'email est obligatoire.");
        }
        return patientRepository.findByEmailIgnoreCase(normalized)
                .map(existing -> {
                    boolean changed = false;
                    if (userId != null && !userId.equals(existing.getUserId())) {
                        existing.setUserId(userId);
                        changed = true;
                    }
                    if ((existing.getPrenom() == null || existing.getPrenom().isBlank())
                            && prenom != null && !prenom.isBlank()) {
                        existing.setPrenom(prenom.trim());
                        changed = true;
                    }
                    if ((existing.getNom() == null || existing.getNom().isBlank())
                            && nom != null && !nom.isBlank()) {
                        existing.setNom(nom.trim());
                        changed = true;
                    }
                    return changed ? patientRepository.save(existing) : existing;
                })
                .orElseGet(() -> {
                    Patient p = new Patient();
                    p.setEmail(normalized);
                    p.setPrenom(prenom != null && !prenom.isBlank() ? prenom.trim() : null);
                    p.setNom(nom != null && !nom.isBlank() ? nom.trim() : null);
                    p.setUserId(userId);
                    Patient saved = patientRepository.save(p);
                    log.info("Patient dossier créé pour le compte email={} idPatient={} userId={}",
                            normalized, saved.getIdPatient(), saved.getUserId());
                    return saved;
                });
    }

    public Patient ajouterPatient(Patient patient) {
        patient.setIdPatient(null);
        return patientRepository.save(patient);
    }

    public Patient modifierPatient(Long id, Patient patient) {
        Patient existant = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient introuvable (id=" + id + ")"));
        
        patient.setIdPatient(id);
        return patientRepository.save(patient);
    }

    public void supprimerPatient(Long id) {
        patientRepository.deleteById(id);
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id).orElse(null);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
}
