package com.example.consultation_microservice.repositories;

import com.example.consultation_microservice.entities.DossierMedical;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DossierMedicalRepository extends JpaRepository<DossierMedical, Long> {

    // ✅ Find the dossier by patient ID
    Optional<DossierMedical> findByPatientId(Long patientId);
}