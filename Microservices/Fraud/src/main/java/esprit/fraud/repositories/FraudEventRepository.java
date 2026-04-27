package esprit.fraud.repositories;

import esprit.fraud.entities.FraudEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudEventRepository extends JpaRepository<FraudEvent, Long> {
    List<FraudEvent> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<FraudEvent> findBySuspiciousTrueOrderByCreatedAtDesc();
}
