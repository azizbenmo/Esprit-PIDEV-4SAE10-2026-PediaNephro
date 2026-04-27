package esprit.reclamation.repositories;

import esprit.reclamation.entities.CsatEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CsatEvaluationRepository extends JpaRepository<CsatEvaluation, Long> {

    Optional<CsatEvaluation> findByReclamationId(Long reclamationId);

    @Query("SELECT AVG(e.note) FROM CsatEvaluation e")
    Double findAverageNote();
}
