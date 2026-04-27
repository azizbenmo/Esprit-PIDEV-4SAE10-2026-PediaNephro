package com.pedianephro.subscription.service;

import com.pedianephro.subscription.dto.AdjustmentProposalResponse;
import com.pedianephro.subscription.dto.PatientProfileRequest;
import com.pedianephro.subscription.dto.RecommendationResponse;
import com.pedianephro.subscription.entity.PatientProfile;
import com.pedianephro.subscription.entity.Subscription;
import com.pedianephro.subscription.entity.SubscriptionPlan;
import com.pedianephro.subscription.entity.SubscriptionStatus;
import com.pedianephro.subscription.repository.PatientProfileRepository;
import com.pedianephro.subscription.repository.SubscriptionPlanRepository;
import com.pedianephro.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdjustmentServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private AdjustmentService adjustmentService;

    @Test
    void check_shouldUseActiveSubscriptionPlan_whenExists() {
        SubscriptionPlan pro = new SubscriptionPlan();
        pro.setId(3L);
        pro.setName("Pro");
        pro.setPrice(300.0);

        Subscription active = new Subscription();
        active.setId(10L);
        active.setUserId(1L);
        active.setStatus(SubscriptionStatus.ACTIVE);
        active.setPlan(pro);

        PatientProfile profile = new PatientProfile();
        profile.setUserId(1L);
        profile.setAgeEnfant(5);
        profile.setMoisDepuisGreffe(10);

        RecommendationResponse reco = RecommendationResponse.builder()
                .planId(2L)
                .planName("Premium")
                .planPrice(200.0)
                .confidenceScore(95.0)
                .justification("OK")
                .build();

        SubscriptionPlan premium = new SubscriptionPlan();
        premium.setId(2L);
        premium.setName("Premium");
        premium.setPrice(200.0);

        when(subscriptionRepository.findTopByUserIdAndStatusOrderByEndDateDesc(1L, SubscriptionStatus.ACTIVE)).thenReturn(active);
        when(patientProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(recommendationService.recommend(any(PatientProfileRequest.class))).thenReturn(reco);
        when(subscriptionPlanRepository.findById(2L)).thenReturn(Optional.of(premium));

        AdjustmentProposalResponse res = adjustmentService.check(1L);

        assertNotNull(res);
        assertEquals(1L, res.getUserId());
        assertEquals(3L, res.getPlanActuelId());
        assertEquals(2L, res.getPlanRecommandeId());
        assertEquals("Premium", res.getPlanRecommandeName());
        verify(recommendationService).recommend(any(PatientProfileRequest.class));
    }

    @Test
    void check_shouldFallbackToDefaultPlan_whenNoActiveSubscription() {
        SubscriptionPlan basique = new SubscriptionPlan();
        basique.setId(1L);
        basique.setName("Basique");
        basique.setPrice(100.0);

        SubscriptionPlan premium = new SubscriptionPlan();
        premium.setId(2L);
        premium.setName("Premium");
        premium.setPrice(200.0);

        when(subscriptionRepository.findTopByUserIdAndStatusOrderByEndDateDesc(1L, SubscriptionStatus.ACTIVE)).thenReturn(null);
        when(subscriptionPlanRepository.findAll()).thenReturn(List.of(premium, basique));
        when(patientProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(subscriptionPlanRepository.findById(1L)).thenReturn(Optional.of(basique));

        AdjustmentProposalResponse res = adjustmentService.check(1L);

        assertNotNull(res);
        assertEquals(1L, res.getPlanActuelId());
        assertEquals(1L, res.getPlanRecommandeId());
        assertEquals("Basique", res.getPlanActuelName());
        assertEquals("Basique", res.getPlanRecommandeName());
        assertTrue(res.getJustification().startsWith("Aucun profil patient"));
        verify(recommendationService, never()).recommend(any());
        verify(subscriptionPlanRepository).findAll();
        verify(subscriptionPlanRepository).findById(eq(1L));
    }
}

