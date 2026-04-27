package com.pedianephro.subscription.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskScoreSchedulerTest {

    @Mock
    private RiskScoreService riskScoreService;

    @InjectMocks
    private RiskScoreScheduler riskScoreScheduler;

    @Test
    void runNightlyRiskCalculation_shouldInvokeService() {
        riskScoreScheduler.runNightlyRiskCalculation();
        verify(riskScoreService).calculateAllActiveSubscriptions();
    }

    @Test
    void runNightlyRiskCalculation_shouldNotThrow_whenServiceFails() {
        doThrow(new RuntimeException("boom")).when(riskScoreService).calculateAllActiveSubscriptions();
        riskScoreScheduler.runNightlyRiskCalculation();
        verify(riskScoreService).calculateAllActiveSubscriptions();
    }
}

