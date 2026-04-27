package com.pedianephro.subscription.repository;

import com.pedianephro.subscription.entity.SubscriptionAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionAuditRepository extends JpaRepository<SubscriptionAudit, Long> {

    List<SubscriptionAudit> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);
}

