package com.example.consultation_microservice.repositories;
import com.example.consultation_microservice.entities.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Long> {

    List<Avis> findByMedecinId(Long medecinId);

    // Vérifie si le patient a déjà noté ce médecin pour cette consultation
    Optional<Avis> findByConsultationId(Long consultationId);

    // Moyenne des notes pour un médecin
    @Query("SELECT AVG(a.note) FROM Avis a WHERE a.medecin.id = :medecinId")
    Double findAverageNoteByMedecinId(Long medecinId);

    // Nombre d'avis pour un médecin
    long countByMedecinId(Long medecinId);

    /** Médecins ayant au moins un avis (évite findAll + lazy / médecin null). */
    @Query("SELECT DISTINCT m.id FROM Avis a JOIN a.medecin m")
    List<Long> findDistinctMedecinIds();
}