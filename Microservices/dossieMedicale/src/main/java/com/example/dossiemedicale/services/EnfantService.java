package com.example.dossiemedicale.services;

import com.example.dossiemedicale.DTO.EnfantRequest;
import com.example.dossiemedicale.entities.Enfant;
import com.example.dossiemedicale.entities.Patient;
import com.example.dossiemedicale.repositoories.EnfantRepository;
import com.example.dossiemedicale.repositoories.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EnfantService {

    private final EnfantRepository enfantRepository;
    private final PatientRepository patientRepository;
    private final RestTemplate restTemplate;

    @Value("${microservice.user.url:http://localhost:8081}")
    private String userServiceUrl;

    public Enfant ajouterEnfant(EnfantRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient introuvable id=" + request.getPatientId()));

        Enfant enfant = new Enfant();
        enfant.setNom(request.getNom());
        enfant.setPrenom(request.getPrenom());
        enfant.setAge(request.getAge());
        enfant.setSexe(request.getSexe());
        enfant.setTaille(request.getTaille());
        enfant.setPoids(request.getPoids());
        enfant.setDateNaissance(request.getDateNaissance());
        enfant.setPatient(patient);

        Enfant saved = enfantRepository.save(enfant);

        // Synchronisation vers le microservice User
        try {
            syncEnfantToUserMicroservice(saved, patient);
        } catch (Exception e) {
            System.err.println("Sync User MS échouée pour enfant id="
                    + saved.getIdEnfant() + " : " + e.getMessage());
        }

        return saved;
    }

    public Enfant modifierEnfant(Long id, EnfantRequest request) {
        Enfant enfant = enfantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enfant introuvable id=" + id));

        if (request.getNom() != null)          enfant.setNom(request.getNom());
        if (request.getPrenom() != null)       enfant.setPrenom(request.getPrenom());
        if (request.getAge() != null)          enfant.setAge(request.getAge());
        if (request.getSexe() != null)         enfant.setSexe(request.getSexe());
        if (request.getTaille() != null)       enfant.setTaille(request.getTaille());
        if (request.getPoids() != null)        enfant.setPoids(request.getPoids());
        if (request.getDateNaissance() != null) enfant.setDateNaissance(request.getDateNaissance());

        if (request.getPatientId() != null) {
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient introuvable id=" + request.getPatientId()));
            enfant.setPatient(patient);
        }

        return enfantRepository.save(enfant);
    }

    public void supprimerEnfant(Long id) {
        if (!enfantRepository.existsById(id)) {
            throw new RuntimeException("Enfant introuvable id=" + id);
        }
        enfantRepository.deleteById(id);
    }

    public List<Enfant> getAllEnfants() {
        return enfantRepository.findAll();
    }

    public List<Enfant> getEnfantsByPatient(Long patientId) {
        return enfantRepository.findByPatient_IdPatient(patientId);
    }

    public Enfant getEnfantById(Long id) {
        return enfantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enfant introuvable id=" + id));
    }

    private void syncEnfantToUserMicroservice(Enfant enfant, Patient patient) {
        if (patient.getUserId() == null || patient.getUserId() <= 0) {
            throw new IllegalStateException(
                    "Impossible de synchroniser l'enfant: userId parent introuvable pour patient dossier id="
                            + patient.getIdPatient());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("fullName", enfant.getPrenom() + " " + enfant.getNom());
        body.put("birthDate", enfant.getDateNaissance() != null
                ? enfant.getDateNaissance().toString() : null);
        body.put("gender", enfant.getSexe());
        body.put("parentId", patient.getUserId());

        org.springframework.http.ResponseEntity<Map> response = restTemplate.postForEntity(
                userServiceUrl + "/mic1/patients/sync-from-dossier",
                body,
                Map.class
        );

        if (response.getBody() != null && response.getBody().get("id") != null) {
            Long userPatientId = ((Number) response.getBody().get("id")).longValue();
            enfant.setUserPatientId(userPatientId);
            enfantRepository.save(enfant);
        }
    }
}