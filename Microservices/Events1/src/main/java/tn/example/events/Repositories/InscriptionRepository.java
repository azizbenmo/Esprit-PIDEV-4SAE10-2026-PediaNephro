package tn.example.events.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.example.events.Entities.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    long countByEventAndStatutNot(Event event, Statut statut);

    long countByEventAndStatut(Event event, Statut statut);


    Optional<Inscription> findFirstByEventAndStatutOrderByDateInscriptionAsc(
            Event event, Statut statut
    );

    List<Inscription> findByEventAndStatutOrderByDateInscriptionAsc(
            Event event, Statut statut
    );

    List<Inscription> findByEventAndStatut(Event event, Statut statut);

    // Recherche par nom/prénom participant
    @Query("SELECT i FROM Inscription i WHERE " +
            "LOWER(i.participant.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.participant.prenom) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Inscription> searchByParticipant(@Param("search") String search);

    // Filtrer par événement
    List<Inscription> findByEvent_IdEvent(Long idEvent);

    // Filtrer par statut
    List<Inscription> findByStatut(Statut statut);

    // Filtrer par date d'inscription
    List<Inscription> findByDateInscriptionBetween(LocalDateTime debut, LocalDateTime fin);

    // Filtrer par type participant
    List<Inscription> findByTypeParticipant(TypeParticipant type);

    @Query("SELECT i FROM Inscription i " +
            "LEFT JOIN FETCH i.event e " +
            "LEFT JOIN FETCH i.participant p " +
            "WHERE (:idEvent IS NULL OR e.idEvent = :idEvent) AND " +
            "(:statut IS NULL OR i.statut = :statut) AND " +
            "(:type IS NULL OR i.typeParticipant = :type) AND " +
            "(:search IS NULL OR " +
            "LOWER(p.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(CONCAT(p.prenom, ' ', p.nom)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(CONCAT(p.nom, ' ', p.prenom)) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:dateDebut IS NULL OR i.dateInscription >= :dateDebut) AND " +
            "(:dateFin IS NULL OR i.dateInscription <= :dateFin)")
    List<Inscription> filterInscriptions(
            @Param("idEvent") Long idEvent,
            @Param("statut") Statut statut,
            @Param("type") TypeParticipant type,
            @Param("search") String search,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin
    );

    List<Inscription> findByParticipant(Participant participant);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Inscription i " +
            "WHERE i.event.idEvent = :eventId AND LOWER(TRIM(i.participant.email)) = LOWER(TRIM(:email)) " +
            "AND i.statut <> :annule")
    boolean existsNonCancelledForEventAndParticipantEmail(
            @Param("eventId") Long eventId,
            @Param("email") String email,
            @Param("annule") Statut annule);
}
