package com.example.consultation_microservice.services;

import com.example.consultation_microservice.entities.DossierMedical;
import com.example.consultation_microservice.entities.RapportConsultation;
import com.example.consultation_microservice.repositories.DossierMedicalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DossierMedicalService {

    @Autowired
    private DossierMedicalRepository dossierMedicalRepository;

    /**
     * ✅ Attaches a RapportConsultation to the patient's DossierMedical.
     * Creates the dossier if it does not exist yet.
     *
     * @param patientId the ID of the patient
     * @param rapport   the saved RapportConsultation
     */
    public Long ajouterRapportAuDossier(Long patientId, RapportConsultation rapport) {
        // Find existing dossier or create a new one for this patient
        DossierMedical dossier = dossierMedicalRepository
                .findByPatientId(patientId)
                .orElseGet(() -> {
                    DossierMedical nouveau = new DossierMedical();
                    nouveau.setPatientId(patientId);
                    return dossierMedicalRepository.save(nouveau);
                });

        // Add the rapport to the dossier's list and save
        dossier.getRapports().add(rapport);
        dossierMedicalRepository.save(dossier);
        return dossier.getId();
    }
}
