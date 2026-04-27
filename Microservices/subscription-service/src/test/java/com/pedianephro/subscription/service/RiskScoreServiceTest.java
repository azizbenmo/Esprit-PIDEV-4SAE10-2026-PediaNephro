package com.pedianephro.subscription.service;

import com.pedianephro.subscription.dto.RiskScoreResponse;
import com.pedianephro.subscription.entity.RiskScoreHistory;
import com.pedianephro.subscription.entity.Subscription;
import com.pedianephro.subscription.entity.SubscriptionStatus;
import com.pedianephro.subscription.entity.UserBehavior;
import com.pedianephro.subscription.repository.RiskScoreHistoryRepository;
import com.pedianephro.subscription.repository.SubscriptionRepository;
import com.pedianephro.subscription.repository.UserBehaviorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskScoreServiceTest {

    @Mock
    private UserBehaviorRepository userBehaviorRepository;

    @Mock
    private RiskScoreHistoryRepository riskScoreHistoryRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private RiskScoreService riskScoreService;

    @BeforeEach
    void setup() {
        when(riskScoreHistoryRepository.save(any(RiskScoreHistory.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void calculateRiskScore_shouldCreateBehaviorWhenMissing_andReturnLowRisk() {
        when(subscriptionRepository.findAll()).thenReturn(List.of());
        when(userBehaviorRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userBehaviorRepository.save(any(UserBehavior.class))).thenAnswer(inv -> inv.getArgument(0));
        when(riskScoreHistoryRepository.findTop30ByUserIdOrderByCalculatedAtDesc(1L)).thenReturn(List.of());

        RiskScoreResponse res = riskScoreService.calculateRiskScore(1L);

        assertNotNull(res);
        assertEquals(1L, res.getUserId());
        assertEquals(0.0, res.getScore());
        assertEquals("FAIBLE", res.getRiskLevel());
        assertNotNull(res.getCalculatedAt());
        verify(userBehaviorRepository).save(any(UserBehavior.class));
    }

    @Test
    void calculateRiskScore_shouldClampScoreTo100_andReturnHighRisk() {
        Subscription active = new Subscription();
        active.setId(99L);
        active.setUserId(2L);
        active.setStatus(SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findAll()).thenReturn(List.of(active));

        UserBehavior b = UserBehavior.builder()
                .userId(2L)
                .joursSansConnexion(20)
                .bilansEnRetard(10)
                .rappelsIgnores(10)
                .rendezVousAnnules(10)
                .medicamentsNonConfirmes(10)
                .build();
        when(userBehaviorRepository.findByUserId(2L)).thenReturn(Optional.of(b));

        RiskScoreHistory h = RiskScoreHistory.builder()
                .userId(2L)
                .score(100.0)
                .riskLevel("ÉLEVÉ")
                .actionRecommandee("Alerte urgente au néphrologue + contact téléphonique recommandé.")
                .build();
        h.setCalculatedAt(LocalDateTime.now());
        when(riskScoreHistoryRepository.findTop30ByUserIdOrderByCalculatedAtDesc(2L)).thenReturn(List.of(h));

        RiskScoreResponse res = riskScoreService.calculateRiskScore(2L);

        assertNotNull(res);
        assertEquals(100.0, res.getScore());
        assertEquals("ÉLEVÉ", res.getRiskLevel());
        assertEquals("Alerte urgente au néphrologue + contact téléphonique recommandé.", res.getActionRecommandee());
    }
}

