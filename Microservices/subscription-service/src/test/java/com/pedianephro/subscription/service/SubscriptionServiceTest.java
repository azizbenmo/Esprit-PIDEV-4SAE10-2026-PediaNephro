package com.pedianephro.subscription.service;

import com.pedianephro.subscription.dto.AutoRenewToggleRequest;
import com.pedianephro.subscription.dto.SubscriptionCreateRequest;
import com.pedianephro.subscription.entity.PromoCode;
import com.pedianephro.subscription.entity.Subscription;
import com.pedianephro.subscription.entity.SubscriptionPlan;
import com.pedianephro.subscription.entity.SubscriptionStatus;
import com.pedianephro.subscription.integration.UserMsValidator;
import com.pedianephro.subscription.repository.SubscriptionPlanRepository;
import com.pedianephro.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private AdjustmentService adjustmentService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SubscriptionAuditService subscriptionAuditService;

    @Mock
    private PromoCodeService promoCodeService;

    @Mock
    private UserMsValidator userMsValidator;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void createSubscriptionForClient_shouldThrowBadRequest_whenDurationInvalid() {
        SubscriptionCreateRequest req = new SubscriptionCreateRequest();
        req.setUserId(1L);
        req.setPlanId(1L);
        req.setUserEmail("a@b.com");
        req.setDurationMonths(0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> subscriptionService.createSubscriptionForClient(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createSubscriptionForClient_shouldThrowConflict_whenActiveNotExpiringSoon() {
        Subscription existing = new Subscription();
        existing.setId(10L);
        existing.setUserId(1L);
        existing.setStatus(SubscriptionStatus.ACTIVE);
        existing.setEndDate(LocalDate.now().plusDays(10));

        when(subscriptionRepository.findByUserId(1L)).thenReturn(List.of(existing));

        SubscriptionCreateRequest req = new SubscriptionCreateRequest();
        req.setUserId(1L);
        req.setPlanId(1L);
        req.setUserEmail("a@b.com");
        req.setDurationMonths(1);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> subscriptionService.createSubscriptionForClient(req));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createSubscriptionForClient_shouldRenew_whenActiveExpiringSoon() {
        Subscription existing = new Subscription();
        existing.setId(10L);
        existing.setUserId(1L);
        existing.setStatus(SubscriptionStatus.ACTIVE);
        existing.setEndDate(LocalDate.now().plusDays(2));

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setId(1L);
        plan.setName("Basique");
        plan.setPrice(100.0);
        plan.setDurationMonths(1);

        when(subscriptionRepository.findByUserId(1L)).thenReturn(List.of(existing));
        when(subscriptionPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionCreateRequest req = new SubscriptionCreateRequest();
        req.setUserId(1L);
        req.setPlanId(1L);
        req.setUserEmail("a@b.com");
        req.setUserFullName("Test");
        req.setDurationMonths(1);
        req.setAutoRenew(true);
        req.setPaymentMethod("CARD");

        Subscription created = subscriptionService.createSubscriptionForClient(req);

        assertNotNull(created);
        assertEquals(SubscriptionStatus.ACTIVE, created.getStatus());
        assertEquals(existing.getEndDate().plusDays(1), created.getStartDate());
        assertEquals(created.getStartDate().plusMonths(1), created.getEndDate());

        verify(subscriptionRepository, atLeastOnce()).save(any(Subscription.class));
        verify(emailService).sendSubscriptionConfirmation(eq("a@b.com"), eq("Test"), eq("Basique"), eq("100.0 DT"));
        verify(notificationService).createNotification(eq(1L), eq("Abonnement activé"), anyString(), eq("SUCCESS"));
        verify(subscriptionAuditService).log(eq(1L), any(), eq("SUBSCRIBED"), eq("Plan: Basique"));
    }

    @Test
    void createSubscriptionForClient_shouldApplyPromoOnTotalDuration_whenPromoProvided() {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setId(2L);
        plan.setName("Premium");
        plan.setPrice(239.0);
        plan.setDurationMonths(1);

        PromoCode promo = new PromoCode();
        promo.setId(1L);
        promo.setCode("PEDIA2026");
        promo.setDiscountPercent(30.0);
        promo.setActive(true);

        when(subscriptionRepository.findByUserId(90L)).thenReturn(List.of());
        when(subscriptionPlanRepository.findById(2L)).thenReturn(Optional.of(plan));
        when(promoCodeService.consumePromoCode("PEDIA2026")).thenReturn(promo);
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionCreateRequest req = new SubscriptionCreateRequest();
        req.setUserId(90L);
        req.setPlanId(2L);
        req.setUserEmail("patient.test@pedianephro.com");
        req.setUserFullName("Patient Test");
        req.setDurationMonths(3);
        req.setPromoCode("PEDIA2026");
        req.setAutoRenew(true);

        Subscription created = subscriptionService.createSubscriptionForClient(req);

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        Subscription saved = captor.getValue();

        assertEquals("PEDIA2026", saved.getPromoCodeUsed());
        double expectedMonthly = (239.0 * 3 * 0.70) / 3.0;
        assertEquals(expectedMonthly, saved.getDiscountedMonthlyPrice(), 0.0001);
        assertEquals(SubscriptionStatus.ACTIVE, saved.getStatus());
        assertEquals(LocalDate.now(), saved.getStartDate());
    }

    @Test
    void updateAutoRenew_shouldThrowConflict_whenSubscriptionNotActive() {
        Subscription s = new Subscription();
        s.setId(43L);
        s.setUserId(1L);
        s.setStatus(SubscriptionStatus.EXPIRED);

        when(subscriptionRepository.findById(43L)).thenReturn(Optional.of(s));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> subscriptionService.updateAutoRenew(43L, new AutoRenewToggleRequest(false)));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void updateAutoRenew_shouldNormalizeEmailAndPersist_whenActive() {
        Subscription s = new Subscription();
        s.setId(43L);
        s.setUserId(1L);
        s.setStatus(SubscriptionStatus.ACTIVE);
        s.setUserEmail("bad-email");

        when(subscriptionRepository.findById(43L)).thenReturn(Optional.of(s));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        Subscription updated = subscriptionService.updateAutoRenew(43L, new AutoRenewToggleRequest(false));

        assertNotNull(updated);
        assertFalse(updated.isAutoRenew());
        assertEquals("user1@pedianephro.com", updated.getUserEmail());
        verify(notificationService).createNotification(eq(1L), eq("Renouvellement automatique"), anyString(), eq("INFO"));
        verify(subscriptionAuditService).log(eq(1L), eq(43L), eq("AUTO_RENEW_TOGGLED"), eq("autoRenew=false"));
    }
}
