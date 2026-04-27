package com.example.consultation_microservice.repositories;

import com.example.consultation_microservice.entities.RapportConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RapportConsultationRepository extends JpaRepository<RapportConsultation, Long> {
    Optional<RapportConsultation> findByConsultationId(Long consultationId);

    @Query("""
            SELECT r FROM RapportConsultation r
            JOIN FETCH r.consultation c
            LEFT JOIN FETCH c.patient
            LEFT JOIN FETCH c.medecin
            WHERE r.dossierMedicalId = :dossierMedicalId
              AND r.estSoumis = true
            ORDER BY r.createdAt DESC, r.id DESC
            """)
    List<RapportConsultation> findSubmittedByDossierMedicalId(@Param("dossierMedicalId") Long dossierMedicalId);

    @Query("""
            SELECT r FROM RapportConsultation r
            JOIN FETCH r.consultation c
            LEFT JOIN FETCH c.patient p
            LEFT JOIN FETCH c.medecin
            WHERE r.estSoumis = true
              AND (p.userId = :patientUserId OR p.id = :patientUserId)
            ORDER BY r.createdAt DESC, r.id DESC
            """)
    List<RapportConsultation> findSubmittedByPatientUserId(@Param("patientUserId") Long patientUserId);
}
