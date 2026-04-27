package tn.example.events.Repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import tn.example.events.Entities.Siege;
import tn.example.events.Entities.Event;
import java.util.List;
import java.util.Optional;

public interface SiegeRepository extends JpaRepository<Siege, Long> {
    List<Siege> findByEventOrderByRangeeAscPositionAsc(Event event);
    Optional<Siege> findByEventAndNumero(Event event, String numero);
    long countByEventAndStatut(Event event, tn.example.events.Entities.StatutSiege statut);
    Optional<Siege> findByInscription_IdInscription(Long inscriptionId);

}
