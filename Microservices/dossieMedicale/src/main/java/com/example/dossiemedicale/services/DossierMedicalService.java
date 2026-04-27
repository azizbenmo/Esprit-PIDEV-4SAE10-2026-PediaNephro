package com.example.dossiemedicale.services;

import com.example.dossiemedicale.DTO.DossierMedicalRequest;
import com.example.dossiemedicale.entities.DossierMedical;
import com.example.dossiemedicale.entities.Enfant;
import com.example.dossiemedicale.repositoories.DossierMedicalRepository;
import com.example.dossiemedicale.repositoories.EnfantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class DossierMedicalService {

    private final DossierMedicalRepository dossierMedicalRepository;
    private final EnfantRepository enfantRepository;

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Random RANDOM = new Random();

    public DossierMedical ajouterDossierMedical(DossierMedicalRequest request) {
        Enfant enfant = enfantRepository.findById(request.getEnfantId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Enfant introuvable (id=" + request.getEnfantId() + ")"
                ));

        if (enfant.getDossierMedical() != null) {
            throw new IllegalStateException("Cet enfant possède déjà un dossier médical.");
        }

        DossierMedical dossier = new DossierMedical();
        dossier.setCode(generateUniqueCode());
        dossier.setDateCreation(request.getDateCreation());
        dossier.setEnfant(enfant);

        return dossierMedicalRepository.save(dossier);
    }

    public DossierMedical modifierDossierMedical(Long id, DossierMedicalRequest request) {
        DossierMedical dossier = dossierMedicalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable (id=" + id + ")"));

        Enfant enfant = enfantRepository.findById(request.getEnfantId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Enfant introuvable (id=" + request.getEnfantId() + ")"
                ));

        if (enfant.getDossierMedical() != null
                && !enfant.getDossierMedical().getIdDossier().equals(dossier.getIdDossier())) {
            throw new IllegalStateException("Cet enfant possède déjà un autre dossier médical.");
        }

        // On garde le même code lors de la modification
        dossier.setDateCreation(request.getDateCreation());
        dossier.setEnfant(enfant);

        return dossierMedicalRepository.save(dossier);
    }

    public void supprimerDossierMedical(Long id) {
        dossierMedicalRepository.deleteById(id);
    }

    public DossierMedical getDossierMedicalById(Long id) {
        return dossierMedicalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable (id=" + id + ")"));
    }

    public List<DossierMedical> getAllDossiersMedicaux() {
        return dossierMedicalRepository.findAll();
    }

    public List<DossierMedical> getDossiersByPatientId(Long patientId) {
        if (patientId == null || patientId <= 0) {
            return List.of();
        }
        return dossierMedicalRepository.findAllFetchedByPatientId(patientId);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateCode();
        } while (dossierMedicalRepository.existsByCode(code));

        return code;
    }

    private String generateCode() {
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            letters.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
        }

        int number = RANDOM.nextInt(1000); // 0 à 999

        return String.format("%s-%03d", letters, number);
    }
}