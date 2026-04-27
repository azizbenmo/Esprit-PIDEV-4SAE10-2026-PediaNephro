package com.pedianephro.subscription.service;

import com.pedianephro.subscription.dto.SubscriptionCreateRequest;
import com.pedianephro.subscription.dto.AdminCreateSubscriptionRequest;
import com.pedianephro.subscription.dto.AdjustmentProposalResponse;
import com.pedianephro.subscription.dto.AdjustmentType;
import com.pedianephro.subscription.dto.AutoRenewToggleRequest;
import com.pedianephro.subscription.dto.ConfirmRenewalRequest;
import com.pedianephro.subscription.dto.RenewalProposalResponse;
import com.pedianephro.subscription.dto.SubscriptionPlanStatsResponse;
import com.pedianephro.subscription.dto.SubscriptionUpdateRequest;
import com.pedianephro.subscription.entity.Subscription;
import com.pedianephro.subscription.entity.SubscriptionPlan;
import com.pedianephro.subscription.entity.SubscriptionStatus;
import com.pedianephro.subscription.entity.PromoCode;
import com.pedianephro.subscription.integration.UserMsValidator;
import com.pedianephro.subscription.repository.SubscriptionPlanRepository;
import com.pedianephro.subscription.repository.SubscriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final EmailService emailService;
    private final AdjustmentService adjustmentService;
    private final NotificationService notificationService;
    private final SubscriptionAuditService subscriptionAuditService;
    private final PromoCodeService promoCodeService;
    private final UserMsValidator userMsValidator;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               SubscriptionPlanRepository subscriptionPlanRepository,
                               EmailService emailService,
                               AdjustmentService adjustmentService,
                               NotificationService notificationService,
                               SubscriptionAuditService subscriptionAuditService,
                               PromoCodeService promoCodeService,
                               UserMsValidator userMsValidator) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.emailService = emailService;
        this.adjustmentService = adjustmentService;
        this.notificationService = notificationService;
        this.subscriptionAuditService = subscriptionAuditService;
        this.promoCodeService = promoCodeService;
        this.userMsValidator = userMsValidator;
    }

    // Liste de tous les plans disponibles
    public List<SubscriptionPlan> getAllPlans() {
        return subscriptionPlanRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Subscription> getSubscriptionsByUserId(Long userId) {
        userMsValidator.ensureUserExists(userId);
        return subscriptionRepository.findByUserId(userId);
    }

    // Vue globale pour l'administration
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    // Récupérer un abonnement par son ID
    public Subscription getSubscriptionById(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));
    }

    // Logique de création d'abonnement côté patient
    public Subscription createSubscriptionForClient(SubscriptionCreateRequest request) {
        if (request.getDurationMonths() == null || request.getDurationMonths() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "durationMonths must be >= 1");
        }

        userMsValidator.ensureUserExists(request.getUserId());

        // Un utilisateur ne peut avoir qu'un seul abonnement actif
        // S'il expire dans moins de 7 jours, on autorise le renouvellement
        List<Subscription> activeSubs = subscriptionRepository.findByUserId(request.getUserId()).stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .collect(Collectors.toList());

        Subscription existingActive = activeSubs.isEmpty() ? null : activeSubs.get(0);
        LocalDate startDate = LocalDate.now();

        if (existingActive != null) {
            long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), existingActive.getEndDate());
            
            if (daysUntilExpiry > 7) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "L'utilisateur a déjà un abonnement actif qui n'expire pas bientôt.");
            }
            
            // Le nouvel abonnement commence après l'ancien
            startDate = existingActive.getEndDate().plusDays(1);
        }

        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Plan ID"));

        Subscription subscription = new Subscription();
        subscription.setPlan(plan);
        subscription.setUserId(request.getUserId());
        subscription.setUserEmail(request.getUserEmail());
        subscription.setUserFullName(request.getUserFullName());
        subscription.setAutoRenew(Boolean.TRUE.equals(request.getAutoRenew()));
        subscription.setPaymentMethod(request.getPaymentMethod());

        subscription.setStartDate(startDate);
        subscription.setEndDate(startDate.plusMonths(request.getDurationMonths()));
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        if (request.getPromoCode() != null && !request.getPromoCode().trim().isBlank()) {
            PromoCode consumed = promoCodeService.consumePromoCode(request.getPromoCode());
            subscription.setPromoCodeUsed(consumed.getCode());
            double percent = consumed.getDiscountPercent() == null ? 0.0 : consumed.getDiscountPercent();
            double total = plan.getPrice() * request.getDurationMonths();
            double discountedTotal = total - (total * (percent / 100.0));
            double discountedMonthly = discountedTotal / request.getDurationMonths();
            subscription.setDiscountedMonthlyPrice(round3(discountedMonthly));
        }

        Subscription saved = subscriptionRepository.save(subscription);

        // Si c'est un renouvellement, on expire l'ancien
        if (existingActive != null) {
            existingActive.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(existingActive);
        }

        // Mail de confirmation
        sendAsyncConfirmationEmail(saved, plan, request.getUserEmail());

        notificationService.createNotification(
                saved.getUserId(),
                "Abonnement activé",
                "Votre abonnement " + plan.getName() + " est actif jusqu'au " + saved.getEndDate() + ".",
                "SUCCESS"
        );
        subscriptionAuditService.log(saved.getUserId(), saved.getId(), "SUBSCRIBED", "Plan: " + plan.getName());

        return saved;
    }

    private double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    public Subscription createSubscriptionForAdmin(AdminCreateSubscriptionRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Plan ID"));

        Subscription subscription = new Subscription();
        subscription.setPlan(plan);
        subscription.setUserId(request.getUserId());
        subscription.setUserEmail("user" + request.getUserId() + "@pedianephro.com");
        subscription.setUserFullName("User " + request.getUserId());
        subscription.setAutoRenew(false);
        subscription.setPaymentMethod(request.getPaymentMethod());

        subscription.setStartDate(request.getStartDate());
        subscription.setEndDate(request.getStartDate().plusMonths(plan.getDurationMonths()));
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        Subscription saved = subscriptionRepository.save(subscription);
        notificationService.createNotification(
                saved.getUserId(),
                "Abonnement activé",
                "Votre abonnement " + plan.getName() + " est actif jusqu'au " + saved.getEndDate() + ".",
                "SUCCESS"
        );
        subscriptionAuditService.log(saved.getUserId(), saved.getId(), "ADMIN_SUBSCRIBED", "Plan: " + plan.getName());
        return saved;
    }

    public Subscription suspendSubscription(Long id) {
        Subscription existing = getSubscriptionById(id);

        if (existing.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seul un abonnement ACTIVE peut être suspendu.");
        }

        existing.setStatus(SubscriptionStatus.SUSPENDED);
        existing.setSuspendedAt(LocalDateTime.now());
        Subscription saved = subscriptionRepository.save(existing);
        notificationService.createNotification(
                saved.getUserId(),
                "Abonnement suspendu",
                "Votre abonnement a été suspendu temporairement.",
                "WARNING"
        );
        subscriptionAuditService.log(saved.getUserId(), saved.getId(), "SUSPENDED", null);
        return saved;
    }

    public Subscription resumeSubscription(Long id) {
        Subscription existing = getSubscriptionById(id);

        if (existing.getStatus() != SubscriptionStatus.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seul un abonnement SUSPENDED peut être repris.");
        }

        existing.setStatus(SubscriptionStatus.ACTIVE);
        existing.setSuspendedAt(null);
        Subscription saved = subscriptionRepository.save(existing);
        notificationService.createNotification(
                saved.getUserId(),
                "Abonnement repris",
                "Votre abonnement a été réactivé.",
                "SUCCESS"
        );
        subscriptionAuditService.log(saved.getUserId(), saved.getId(), "RESUMED", null);
        return saved;
    }


    private void sendAsyncConfirmationEmail(Subscription saved, SubscriptionPlan plan, String userEmail) {
        try {
            String amount = plan.getPrice() + " DT";
            
            emailService.sendSubscriptionConfirmation(userEmail, 
                saved.getUserFullName(), 
                plan.getName(), 
                amount);
        } catch (Exception e) {
            // Log l'erreur mais ne pas interrompre le flux principal
            System.err.println("Échec de l'envoi du mail de confirmation : " + e.getMessage());
        }
    }

    /**
     * Mise à jour d'une souscription (gérée côté backoffice / automatisme).
     */
    public Subscription updateSubscription(Long id, SubscriptionUpdateRequest updates) {
        Subscription existing = getSubscriptionById(id);
        Long oldPlanId = existing.getPlan() != null ? existing.getPlan().getId() : null;

        if (updates.getUserId() != null) existing.setUserId(updates.getUserId());
        if (updates.getUserFullName() != null) existing.setUserFullName(updates.getUserFullName());
        if (updates.getPaymentMethod() != null) existing.setPaymentMethod(updates.getPaymentMethod());

        if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
        if (updates.getStartDate() != null) existing.setStartDate(updates.getStartDate());
        if (updates.getEndDate() != null) existing.setEndDate(updates.getEndDate());
        if (updates.getAutoRenew() != null) existing.setAutoRenew(updates.getAutoRenew());

        if (updates.getPlanId() != null) {
            SubscriptionPlan plan = subscriptionPlanRepository.findById(updates.getPlanId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Plan ID"));
            existing.setPlan(plan);

            boolean endDateWasProvided = updates.getEndDate() != null;
            if (!endDateWasProvided && existing.getStartDate() != null) {
                existing.setEndDate(existing.getStartDate().plusMonths(plan.getDurationMonths()));
            }
        }

        if (existing.getStartDate() != null && existing.getEndDate() != null
                && existing.getEndDate().isBefore(existing.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate must be on/after startDate");
        }

        Subscription saved = subscriptionRepository.save(existing);
        Long newPlanId = saved.getPlan() != null ? saved.getPlan().getId() : null;
        if (oldPlanId != null && newPlanId != null && !oldPlanId.equals(newPlanId)) {
            notificationService.createNotification(
                    saved.getUserId(),
                    "Changement de plan",
                    "Votre plan a été modifié vers " + saved.getPlan().getName() + ".",
                    "INFO"
            );
            subscriptionAuditService.log(saved.getUserId(), saved.getId(), "PLAN_CHANGED", "Nouveau plan: " + saved.getPlan().getName());
        }
        return saved;
    }

    public void deleteSubscription(Long id) {
        subscriptionRepository.deleteById(id);
    }

    /**
     * Liste des abonnements par plan pour la vue détaillée "clients d'un type".
     */
    public List<Subscription> getSubscriptionsByPlan(Long planId) {
        return subscriptionRepository.findByPlan_Id(planId);
    }

    /**
     * Statistiques simples pour le dashboard admin : chaque plan
     * avec le nombre total de souscriptions associées.
     */
    public List<SubscriptionPlanStatsResponse> getPlansWithStats() {
        return subscriptionPlanRepository.findAll()
                .stream()
                .map(plan -> {
                    long total = subscriptionRepository.countByPlan_Id(plan.getId());
                    return new SubscriptionPlanStatsResponse(plan, total);
                })
                .collect(Collectors.toList());
    }

    // Envoi automatique des rappels d'expiration (tous les matins)
    @Scheduled(cron = "0 0 8 * * *")
    public void checkExpiringSubscriptions() {
        LocalDate reminderDate = LocalDate.now().plusDays(7);
        
        List<Subscription> expiringSoon = subscriptionRepository
                .findByStatusAndEndDateBetweenAndReminderSentFalse(
                        SubscriptionStatus.ACTIVE, 
                        LocalDate.now(), 
                        reminderDate
                );

        for (Subscription sub : expiringSoon) {
            sendReminder(sub);
        }

        List<Subscription> expiredAutoRenew = subscriptionRepository
                .findByStatusAndEndDateBeforeAndAutoRenewTrue(
                        SubscriptionStatus.ACTIVE,
                        LocalDate.now()
                );

        for (Subscription sub : expiredAutoRenew) {
            try {
                processAutoRenewAfterExpiration(sub);
            } catch (Exception e) {
            }
        }
    }

    /**
     * Envoie un rappel pour un abonnement spécifique s'il remplit les conditions
     * (moins de 7 jours avant expiration et rappel non déjà envoyé).
     */
    public void sendReminderIfNecessary(Long id) {
        subscriptionRepository.findById(id).ifPresent(sub -> {
            LocalDate reminderDate = LocalDate.now().plusDays(7);
            boolean isExpiringSoon = sub.getStatus() == SubscriptionStatus.ACTIVE 
                && sub.getEndDate() != null
                && !sub.getEndDate().isBefore(LocalDate.now())
                && !sub.getEndDate().isAfter(reminderDate);

            if (isExpiringSoon && !sub.isReminderSent()) {
                sendReminder(sub);
            }
        });
    }

    private void sendReminder(Subscription sub) {
        try {
            emailService.sendExpirationReminder(
                    sub.getUserEmail(), 
                    sub.getUserFullName(), 
                    sub.getPlan().getName(), 
                    sub.getEndDate()
            );
            
            sub.setReminderSent(true);
            subscriptionRepository.save(sub);

            notificationService.createNotification(
                    sub.getUserId(),
                    "Abonnement bientôt expiré",
                    "Votre abonnement " + sub.getPlan().getName() + " expire le " + sub.getEndDate() + ".",
                    "WARNING"
            );
            subscriptionAuditService.log(sub.getUserId(), sub.getId(), "EXPIRATION_REMINDER_SENT", "Fin: " + sub.getEndDate());
        } catch (Exception e) {
            // Log simple en cas d'erreur
        }
    }

    public Subscription updateAutoRenew(Long subscriptionId, AutoRenewToggleRequest request) {
        Subscription existing = getSubscriptionById(subscriptionId);
        if (existing.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seul un abonnement ACTIVE peut être modifié.");
        }

        existing.setUserEmail(resolveUserEmail(existing));
        existing.setAutoRenew(Boolean.TRUE.equals(request.getAutoRenew()));
        Subscription saved = subscriptionRepository.save(existing);

        notificationService.createNotification(
                saved.getUserId(),
                "Renouvellement automatique",
                saved.isAutoRenew()
                        ? "Le renouvellement automatique est activé."
                        : "Le renouvellement automatique est désactivé.",
                "INFO"
        );
        subscriptionAuditService.log(saved.getUserId(), saved.getId(), "AUTO_RENEW_TOGGLED", "autoRenew=" + saved.isAutoRenew());
        return saved;
    }

    @Transactional(readOnly = true)
    public RenewalProposalResponse getRenewalProposal(Long subscriptionId) {
        Subscription sub = getSubscriptionById(subscriptionId);
        AdjustmentProposalResponse proposal = adjustmentService.check(sub.getUserId());
        return RenewalProposalResponse.builder()
                .subscriptionId(sub.getId())
                .userId(sub.getUserId())
                .currentEndDate(sub.getEndDate())
                .autoRenew(sub.isAutoRenew())
                .proposal(proposal)
                .build();
    }

    public Subscription confirmRenewal(Long subscriptionId, ConfirmRenewalRequest request) {
        Subscription current = getSubscriptionById(subscriptionId);
        if (current.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seul un abonnement ACTIVE peut être renouvelé.");
        }

        long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), current.getEndDate());
        if (daysUntilExpiry > 7) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le renouvellement est disponible uniquement dans les 7 jours avant l'échéance.");
        }

        AdjustmentProposalResponse proposal = adjustmentService.check(current.getUserId());
        Long targetPlanId = proposal.getPlanActuelId();
        if (Boolean.TRUE.equals(request.getAcceptSuggestion()) && proposal.getTypeAjustement() != AdjustmentType.OPTIMAL) {
            targetPlanId = proposal.getPlanRecommandeId();
        }

        SubscriptionPlan targetPlan = subscriptionPlanRepository.findById(targetPlanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Plan ID"));

        SubscriptionCreateRequest renewal = new SubscriptionCreateRequest();
        renewal.setUserId(current.getUserId());
        renewal.setUserEmail(resolveUserEmail(current));
        renewal.setUserFullName(current.getUserFullName());
        renewal.setPlanId(targetPlanId);
        renewal.setDurationMonths(targetPlan.getDurationMonths());
        renewal.setAutoRenew(current.isAutoRenew());
        renewal.setPaymentMethod(current.getPaymentMethod());

        Subscription created = createSubscriptionForClient(renewal);

        notificationService.createNotification(
                current.getUserId(),
                "Renouvellement confirmé",
                "Votre renouvellement a été enregistré. Nouveau plan : " + created.getPlan().getName() + ".",
                "SUCCESS"
        );
        notificationService.markExpirationReminderAsRead(current.getUserId());
        subscriptionAuditService.log(current.getUserId(), current.getId(), "RENEWAL_CONFIRMED", "Nouveau plan: " + created.getPlan().getName());
        return created;
    }

    private void processAutoRenewAfterExpiration(Subscription sub) {
        if (sub.getEndDate() == null || sub.getPlan() == null) return;
        if (!sub.isAutoRenew()) return;
        if (!sub.getEndDate().isBefore(LocalDate.now())) return;

        SubscriptionCreateRequest renewal = new SubscriptionCreateRequest();
        renewal.setUserId(sub.getUserId());
        renewal.setUserEmail(resolveUserEmail(sub));
        renewal.setUserFullName(sub.getUserFullName());
        renewal.setPlanId(sub.getPlan().getId());
        renewal.setDurationMonths(sub.getPlan().getDurationMonths());
        renewal.setAutoRenew(true);
        renewal.setPaymentMethod(sub.getPaymentMethod());

        Subscription created = createSubscriptionForClient(renewal);

        notificationService.createNotification(
                sub.getUserId(),
                "Renouvellement automatique effectué",
                "Votre abonnement a été renouvelé automatiquement avec le plan " + created.getPlan().getName() + ".",
                "SUCCESS"
        );
        notificationService.markExpirationReminderAsRead(sub.getUserId());
        subscriptionAuditService.log(sub.getUserId(), sub.getId(), "AUTO_RENEWED", "Plan: " + created.getPlan().getName());
    }

    private String resolveUserEmail(Subscription sub) {
        String email = sub.getUserEmail();
        if (email != null) {
            email = email.trim();
        }
        if (email != null && !email.isBlank() && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return email;
        }
        return "user" + sub.getUserId() + "@pedianephro.com";
    }



    public void initData() {
        if (subscriptionPlanRepository.count() == 0) {
            // Plan Basique (ID: 1)
            SubscriptionPlan basique = new SubscriptionPlan();
            basique.setName("Basique");
            basique.setPrice(102.0);
            basique.setDurationMonths(1);
            basique.setDescription("Consultations et dossier médical — sans forum, IA ni événements");
            basique.setRecommended(false);
            basique.setColorTheme("green");
            basique.setFeatures(List.of(
                    "Consultations (demandes et suivi des rendez-vous)",
                    "Dossier médical (parcours dossier, examens, prescriptions liés au dossier)",
                    "Pas d’accès : forum, conseil IA, événements, engagement / ajustement avancés, etc."
            ));

            // Plan Premium (ID: 2)
            SubscriptionPlan premium = new SubscriptionPlan();
            premium.setName("Premium");
            premium.setPrice(239.0);
            premium.setDurationMonths(1);
            premium.setDescription("Basique + forum et conseil intelligent (IA)");
            premium.setRecommended(true);
            premium.setColorTheme("purple");
            premium.setFeatures(List.of(
                    "Même périmètre que le plan Basique (consultations et dossier médical)",
                    "Accès au forum",
                    "Accès au conseil intelligent (IA)"
            ));

            // Plan Pro (ID: 3)
            SubscriptionPlan pro = new SubscriptionPlan();
            pro.setName("Pro");
            pro.setPrice(435.0);
            pro.setDurationMonths(1);
            pro.setDescription("Toute l’application et les microservices (événements, forum, etc.)");
            pro.setRecommended(false);
            pro.setColorTheme("blue");
            pro.setFeatures(List.of(
                    "Accès à toutes les fonctionnalités de l’application",
                    "Accès à l’ensemble des microservices : événements, forum, conseil IA, abonnement / ajustement, etc."
            ));

            subscriptionPlanRepository.saveAll(List.of(basique, premium, pro));
        }

        // Créer un abonnement de test si aucun n'existe
        if (subscriptionRepository.count() == 0) {
            subscriptionPlanRepository.findAll().stream()
                    .filter(p -> p.getName().equals("Pro"))
                    .findFirst()
                    .ifPresent(proPlan -> {
                        Subscription testSub = new Subscription();
                        testSub.setUserId(90L);
                        testSub.setUserEmail("user90@example.com");
                        testSub.setUserFullName("Utilisateur test");
                        testSub.setPlan(proPlan);
                        testSub.setStartDate(LocalDate.now().minusDays(25));
                        testSub.setEndDate(LocalDate.now().plusDays(5)); // Expire bientôt
                        testSub.setStatus(SubscriptionStatus.ACTIVE);
                        testSub.setReminderSent(false);
                        testSub.setAutoRenew(false);
                        subscriptionRepository.save(testSub);
                    });
        }
    }
}
