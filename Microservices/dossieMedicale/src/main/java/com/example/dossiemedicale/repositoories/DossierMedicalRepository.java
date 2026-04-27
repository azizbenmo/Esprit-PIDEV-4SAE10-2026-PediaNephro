package com.example.dossiemedicale.repositoories;

import com.example.dossiemedicale.entities.DossierMedical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DossierMedicalRepository extends JpaRepository<DossierMedical, Long> {
    boolean existsByCode(String code);

    @Query("""
            SELECT DISTINCT d FROM DossierMedical d
            JOIN FETCH d.enfant e
            JOIN FETCH e.patient p
            WHERE p.idPatient = :patientId
            """)
    List<DossierMedical> findAllFetchedByPatientId(@Param("patientId") Long patientId);

    Optional<DossierMedical> findFirstByEnfant_UserPatientId(Long userPatientId);
}
