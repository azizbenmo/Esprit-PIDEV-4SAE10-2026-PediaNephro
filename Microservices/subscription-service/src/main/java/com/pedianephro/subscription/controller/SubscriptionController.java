package com.pedianephro.subscription.controller;

import com.pedianephro.subscription.dto.AdminCreateSubscriptionRequest;
import com.pedianephro.subscription.dto.AutoRenewToggleRequest;
import com.pedianephro.subscription.dto.ConfirmRenewalRequest;
import com.pedianephro.subscription.dto.RenewalProposalResponse;
import com.pedianephro.subscription.dto.SubscriptionCreateRequest;
import com.pedianephro.subscription.dto.SubscriptionPlanStatsResponse;
import com.pedianephro.subscription.dto.SubscriptionUpdateRequest;
import com.pedianephro.subscription.entity.Subscription;
import com.pedianephro.subscription.entity.SubscriptionPlan;
import com.pedianephro.subscription.service.SubscriptionService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/subscriptions", "/subscriptions"})
@CrossOrigin
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostConstruct
    public void init() {
        subscriptionService.initData();
    }

    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlan>> getAllPlans() {
        return ResponseEntity.ok(subscriptionService.getAllPlans());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Subscription>> getSubscriptionsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByUserId(userId));
    }

    @PostMapping("/client")
    public ResponseEntity<Subscription> createSubscriptionForClient(@Valid @RequestBody SubscriptionCreateRequest request) {
        return ResponseEntity.ok(subscriptionService.createSubscriptionForClient(request));
    }

    @GetMapping
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    @PostMapping
    public ResponseEntity<Subscription> createSubscriptionForAdmin(@Valid @RequestBody AdminCreateSubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.createSubscriptionForAdmin(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subscription> getSubscriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @GetMapping("/admin/plans")
    public ResponseEntity<List<SubscriptionPlanStatsResponse>> getPlansWithStats() {
        return ResponseEntity.ok(subscriptionService.getPlansWithStats());
    }

    @GetMapping("/admin/plans/{planId}/subscriptions")
    public ResponseEntity<List<Subscription>> getSubscriptionsByPlan(@PathVariable Long planId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByPlan(planId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subscription> updateSubscription(@PathVariable Long id,
                                                           @Valid @RequestBody SubscriptionUpdateRequest updates) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(id, updates));
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<Subscription> suspendSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.suspendSubscription(id));
    }

    @PutMapping("/{id}/resume")
    public ResponseEntity<Subscription> resumeSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.resumeSubscription(id));
    }

    @PutMapping("/{id}/auto-renew")
    public ResponseEntity<Subscription> updateAutoRenew(@PathVariable Long id, @Valid @RequestBody AutoRenewToggleRequest request) {
        return ResponseEntity.ok(subscriptionService.updateAutoRenew(id, request));
    }

    @GetMapping("/{id}/renewal-proposal")
    public ResponseEntity<RenewalProposalResponse> getRenewalProposal(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getRenewalProposal(id));
    }

    @PostMapping("/{id}/confirm-renewal")
    public ResponseEntity<Subscription> confirmRenewal(@PathVariable Long id, @Valid @RequestBody ConfirmRenewalRequest request) {
        return ResponseEntity.ok(subscriptionService.confirmRenewal(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test-reminders")
    public ResponseEntity<String> triggerReminders() {
        subscriptionService.checkExpiringSubscriptions();
        return ResponseEntity.ok("Vérification des rappels déclenchée !");
    }

    /**
     * Déclenche un rappel pour un abonnement spécifique (utilisé par le Front quand l'alerte s'affiche).
     */
    @PostMapping("/{id}/trigger-reminder")
    public ResponseEntity<Void> triggerReminder(@PathVariable Long id) {
        subscriptionService.sendReminderIfNecessary(id);
        return ResponseEntity.ok().build();
    }
}
