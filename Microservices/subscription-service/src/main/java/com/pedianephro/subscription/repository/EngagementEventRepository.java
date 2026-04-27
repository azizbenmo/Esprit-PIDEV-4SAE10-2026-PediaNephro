package com.pedianephro.subscription.repository;

import com.pedianephro.subscription.entity.EngagementEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EngagementEventRepository extends JpaRepository<EngagementEvent, Long> {

    Optional<EngagementEvent> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime from);

    long countByUserIdAndEventTypeAndCreatedAtAfter(Long userId, String eventType, LocalDateTime from);
}

