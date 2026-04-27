package com.pedianephro.subscription.service;

import com.pedianephro.subscription.entity.SubscriptionAudit;
import com.pedianephro.subscription.repository.SubscriptionAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionAuditService {

    private final SubscriptionAuditRepository subscriptionAuditRepository;

    public void log(Long userId, Long subscriptionId, String action, String details) {
        SubscriptionAudit a = new SubscriptionAudit();
        a.setUserId(userId);
        a.setSubscriptionId(subscriptionId);
        a.setAction(action);
        a.setDetails(details);
        subscriptionAuditRepository.save(a);
    }
}

