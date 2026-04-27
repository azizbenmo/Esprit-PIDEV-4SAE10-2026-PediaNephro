package com.example.dossiemedicale.services;

import com.example.dossiemedicale.DTO.HospitalisationRequest;
import com.example.dossiemedicale.entities.Enfant;
import com.example.dossiemedicale.entities.Hospitalisation;
import com.example.dossiemedicale.repositoories.EnfantRepository;
import com.example.dossiemedicale.repositoories.HospitalisationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HospitalisationService {

    private final HospitalisationRepository hospitalisationRepository;
    private final EnfantRepository enfantRepository;

    public Hospitalisation ajouterHospitalisation(HospitalisationRequest request) {

        Enfant enfant = enfantRepository.findById(request.getEnfantId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Enfant introuvable (id=" + request.getEnfantId() + ")"
                ));

        Hospitalisation hosp = new Hospitalisation();
        hosp.setDateEntree(request.getDateEntree());
        hosp.setDateSortie(request.getDateSortie());
        hosp.setMotif(request.getMotif());
        hosp.setServiceHospitalier(request.getServiceHospitalier());
        hosp.setEnfant(enfant);

        return hospitalisationRepository.save(hosp);
    }

    public Hospitalisation modifierHospitalisation(Long id, HospitalisationRequest request) {
        Hospitalisation hosp = hospitalisationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hospitalisation introuvable (id=" + id + ")"));

        hosp.setDateEntree(request.getDateEntree());
        hosp.setDateSortie(request.getDateSortie());
        hosp.setMotif(request.getMotif());
        hosp.setServiceHospitalier(request.getServiceHospitalier());

        if (request.getEnfantId() != null) {
            Enfant enfant = enfantRepository.findById(request.getEnfantId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Enfant introuvable (id=" + request.getEnfantId() + ")"
                    ));
            hosp.setEnfant(enfant);
        }

        return hospitalisationRepository.save(hosp);
    }

    public void supprimerHospitalisation(Long id) {
        hospitalisationRepository.deleteById(id);
    }

    public Hospitalisation getHospitalisationById(Long id) {
        return hospitalisationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hospitalisation introuvable (id=" + id + ")"));
    }

    public List<Hospitalisation> getAllHospitalisations() {
        return hospitalisationRepository.findAll();
    }
}