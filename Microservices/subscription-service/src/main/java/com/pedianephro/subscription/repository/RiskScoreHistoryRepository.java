package com.pedianephro.subscription.repository;

import com.pedianephro.subscription.entity.RiskScoreHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiskScoreHistoryRepository extends JpaRepository<RiskScoreHistory, Long> {
    List<RiskScoreHistory> findByUserIdOrderByCalculatedAtDesc(Long userId);
    
    // Pour récupérer les 30 derniers
    List<RiskScoreHistory> findTop30ByUserIdOrderByCalculatedAtDesc(Long userId);

    Optional<RiskScoreHistory> findTopByUserIdOrderByCalculatedAtDesc(Long userId);

    List<RiskScoreHistory> findByRiskLevel(String riskLevel);
}
