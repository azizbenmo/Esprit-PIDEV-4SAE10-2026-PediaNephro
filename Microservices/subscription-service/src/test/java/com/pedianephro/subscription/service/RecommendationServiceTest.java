package com.pedianephro.subscription.service;

import com.pedianephro.subscription.dto.PatientProfileRequest;
import com.pedianephro.subscription.dto.RecommendationResponse;
import com.pedianephro.subscription.entity.PatientProfile;
import com.pedianephro.subscription.entity.SubscriptionPlan;
import com.pedianephro.subscription.repository.PatientProfileRepository;
import com.pedianephro.subscription.repository.SubscriptionPlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void recommend_shouldReturnPro_whenHighRiskProfile() {
        SubscriptionPlan basique = new SubscriptionPlan();
        basique.setId(1L);
        basique.setName("Basique");
        basique.setPrice(100.0);

        SubscriptionPlan premium = new SubscriptionPlan();
        premium.setId(2L);
        premium.setName("Premium");
        premium.setPrice(200.0);

        SubscriptionPlan pro = new SubscriptionPlan();
        pro.setId(3L);
        pro.setName("Pro");
        pro.setPrice(300.0);

        when(subscriptionPlanRepository.findById(3L)).thenReturn(Optional.of(pro));
        when(subscriptionPlanRepository.findAll()).thenReturn(List.of(basique, premium, pro));

        PatientProfileRequest req = new PatientProfileRequest();
        req.setUserId(1L);
        req.setAgeEnfant(5);
        req.setMoisDepuisGreffe(2);
        req.setAEuEpisodeRejet(0);
        req.setNombreHospitalisationsAn(0);
        req.setPrendImmunosuppresseurs(1);
        req.setNombreMedicamentsQuotidiens(2);
        req.setPresenceComplicationActive(0);

        RecommendationResponse res = recommendationService.recommend(req);

        assertNotNull(res);
        assertEquals(3L, res.getPlanId());
        assertEquals("Pro", res.getPlanName());
        assertEquals(100.0, res.getConfidenceScore());
        assertNotNull(res.getAllPlansRanked());
        assertFalse(res.getAllPlansRanked().isEmpty());
        assertEquals(3L, res.getAllPlansRanked().get(0).getPlanId());
    }

    @Test
    void saveProfileAndRecommend_shouldPersistProfile() {
        SubscriptionPlan basique = new SubscriptionPlan();
        basique.setId(1L);
        basique.setName("Basique");
        basique.setPrice(100.0);

        when(patientProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(patientProfileRepository.save(any(PatientProfile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(subscriptionPlanRepository.findById(1L)).thenReturn(Optional.of(basique));
        when(subscriptionPlanRepository.findAll()).thenReturn(List.of(basique));

        PatientProfileRequest req = new PatientProfileRequest();
        req.setUserId(10L);
        req.setAgeEnfant(4);
        req.setMoisDepuisGreffe(30);
        req.setAEuEpisodeRejet(0);
        req.setNombreHospitalisationsAn(0);
        req.setPrendImmunosuppresseurs(1);
        req.setNombreMedicamentsQuotidiens(1);
        req.setPresenceComplicationActive(0);

        RecommendationResponse res = recommendationService.saveProfileAndRecommend(req);

        assertNotNull(res);
        verify(patientProfileRepository).save(any(PatientProfile.class));
    }
}

