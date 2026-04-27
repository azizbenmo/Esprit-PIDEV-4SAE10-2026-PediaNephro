package esprit.reclamation.repositories;

import esprit.reclamation.entities.ReclamationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReclamationHistoryRepository extends JpaRepository<ReclamationHistory, Long> {

    List<ReclamationHistory> findByReclamationIdOrderByDateChangementAsc(Long reclamationId);
}
