package esprit.reclamation.repositories;

import esprit.reclamation.entities.Priorite;
import esprit.reclamation.entities.Reclamation;
import esprit.reclamation.entities.StatutReclamation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {

    List<Reclamation> findByUserId(Long userId);

    List<Reclamation> findByStatut(StatutReclamation statut);

    List<Reclamation> findByPriorite(Priorite priorite);

    List<Reclamation> findByUserIdAndStatut(Long userId, StatutReclamation statut);

    long countByStatut(StatutReclamation statut);

    List<Reclamation> findByEscaladeeIsFalseAndSlaDeadlineBeforeAndStatutIn(
            LocalDateTime now, Collection<StatutReclamation> statuts);
}

