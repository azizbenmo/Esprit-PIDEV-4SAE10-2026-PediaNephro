package com.example.consultation_microservice.repositories;

import com.example.consultation_microservice.entities.Consultation;
import com.example.consultation_microservice.entities.ConsultationStatus;
import com.example.consultation_microservice.entities.NiveauUrgence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    List<Consultation> findByPatientId(Long patientId);

    /** Historique : {@code ref} = id local patient OU userId PediaNéphro. */
    @Query("""
        SELECT c FROM Consultation c
        WHERE c.patient.id = :ref OR c.patient.userId = :ref
        """)
    List<Consultation> findConsultationsForPatientReference(@Param("ref") Long ref);

    /**
     * Historique des demandes créées par un parent donné.
     */
    List<Consultation> findByDemandeurParentEmailIgnoreCaseOrderByDateSouhaiteeAsc(String demandeurParentEmail);

    List<Consultation> findByMedecinIdAndStatut(Long medecinId, ConsultationStatus statut);

    int countByMedecinIdAndStatut(Long medecinId, ConsultationStatus statut);

    List<Consultation> findByMedecinId(Long medecinId);

    List<Consultation> findByMedecinUserId(Long userId);

    long countByMedecinIdAndStatutIn(Long medecinId, List<ConsultationStatus> statuts);

    List<Consultation> findByNiveauUrgenceOrderByScoreUrgenceDesc(NiveauUrgence niveauUrgence);

    /**
     * Vérifie si un créneau est déjà pris pour ce médecin.
     */
    @Query("""
        SELECT COUNT(c) > 0 FROM Consultation c
        WHERE c.medecin.id = :medecinId
          AND c.dateSouhaitee = :dateSouhaitee
          AND c.statut NOT IN (
              com.example.consultation_microservice.entities.ConsultationStatus.ANNULEE,
              com.example.consultation_microservice.entities.ConsultationStatus.REFUSEE
          )
    """)
    boolean existsCreneauPris(
            @Param("medecinId") Long medecinId,
            @Param("dateSouhaitee") LocalDateTime dateSouhaitee
    );

    /**
     * ✅ FIX : Comparaison par plage [debutJournee, finJournee) au lieu de CAST AS date
     * qui ne fonctionne pas correctement en JPQL/Hibernate.
     * Appelé avec debutJournee = jour à 00:00:00 et finJournee = jour+1 à 00:00:00
     */
    @Query("""
        SELECT c.dateSouhaitee FROM Consultation c
        WHERE c.medecin.id = :medecinId
          AND c.dateSouhaitee >= :debutJournee
          AND c.dateSouhaitee < :finJournee
          AND c.statut NOT IN (
              com.example.consultation_microservice.entities.ConsultationStatus.ANNULEE,
              com.example.consultation_microservice.entities.ConsultationStatus.REFUSEE
          )
    """)
    List<LocalDateTime> findCreneauxPrisByMedecinAndDate(
            @Param("medecinId") Long medecinId,
            @Param("debutJournee") LocalDateTime debutJournee,
            @Param("finJournee") LocalDateTime finJournee
    );
}
