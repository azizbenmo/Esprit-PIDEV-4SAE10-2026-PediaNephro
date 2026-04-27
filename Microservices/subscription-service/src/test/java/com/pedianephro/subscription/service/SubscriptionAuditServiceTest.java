package com.pedianephro.subscription.service;

import com.pedianephro.subscription.entity.SubscriptionAudit;
import com.pedianephro.subscription.repository.SubscriptionAuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionAuditServiceTest {

    @Mock
    private SubscriptionAuditRepository subscriptionAuditRepository;

    @InjectMocks
    private SubscriptionAuditService subscriptionAuditService;

    @Test
    void log_shouldPersistAuditRow() {
        when(subscriptionAuditRepository.save(any(SubscriptionAudit.class))).thenAnswer(inv -> inv.getArgument(0));

        subscriptionAuditService.log(1L, 10L, "AUTO_RENEWED", "Plan: Pro");

        ArgumentCaptor<SubscriptionAudit> captor = ArgumentCaptor.forClass(SubscriptionAudit.class);
        verify(subscriptionAuditRepository).save(captor.capture());
        SubscriptionAudit saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals(10L, saved.getSubscriptionId());
        assertEquals("AUTO_RENEWED", saved.getAction());
        assertEquals("Plan: Pro", saved.getDetails());
    }
}

