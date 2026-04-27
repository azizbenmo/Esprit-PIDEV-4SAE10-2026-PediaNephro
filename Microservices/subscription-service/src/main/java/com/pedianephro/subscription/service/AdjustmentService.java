package com.pedianephro.subscription.service;

import com.pedianephro.subscription.dto.AdjustmentProposalResponse;
import com.pedianephro.subscription.dto.AdjustmentType;
import com.pedianephro.subscription.dto.PatientProfileRequest;
import com.pedianephro.subscription.dto.RecommendationResponse;
import com.pedianephro.subscription.entity.PatientProfile;
import com.pedianephro.subscription.entity.Subscription;
import com.pedianephro.subscription.entity.SubscriptionPlan;
import com.pedianephro.subscription.entity.SubscriptionStatus;
import com.pedianephro.subscription.repository.PatientProfileRepository;
import com.pedianephro.subscription.repository.SubscriptionPlanRepository;
import com.pedianephro.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdjustmentService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final RecommendationService recommendationService;

    public AdjustmentProposalResponse check(Long userId) {
        Subscription active = subscriptionRepository.findTopByUserIdAndStatusOrderByEndDateDesc(userId, SubscriptionStatus.ACTIVE);
        SubscriptionPlan planActuel = active != null ? active.getPlan() : resolveDefaultPlan();
        RecommendationResponse reco = getRecommendationFromProfile(userId, planActuel);

        SubscriptionPlan planRecommande = subscriptionPlanRepository.findById(reco.getPlanId())
                .orElse(planActuel);

        return buildResponse(userId, planActuel, planRecommande, reco.getJustification(), reco.getConfidenceScore());
    }

    public List<AdjustmentProposalResponse> checkAll() {
        List<Long> userIds = subscriptionRepository.findAll().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .map(Subscription::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        return userIds.stream().map(this::check).collect(Collectors.toList());
    }

    public AdjustmentProposalResponse simulate(Long userId, AdjustmentType scenario) {
        Subscription active = subscriptionRepository.findTopByUserIdAndStatusOrderByEndDateDesc(userId, SubscriptionStatus.ACTIVE);
        SubscriptionPlan planActuel = active != null ? active.getPlan() : resolveDefaultPlan();
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll().stream()
                .sorted(Comparator.comparing(SubscriptionPlan::getPrice))
                .collect(Collectors.toList());

        SubscriptionPlan cible = resolveSimulationTarget(planActuel, plans, scenario);

        String justification;
        Double confidence;

        if (scenario == AdjustmentType.OPTIMAL) {
            justification = "Simulation OPTIMAL : maintien du plan actuel.";
            confidence = 1.0;
        } else if (scenario == AdjustmentType.MONTEE) {
            justification = "Simulation MONTEE : proposition d'un plan plus complet pour intensifier le suivi.";
            confidence = 0.75;
        } else {
            justification = "Simulation DESCENTE : proposition d'un plan plus économique compatible avec un suivi moins intensif.";
            confidence = 0.75;
        }

        return buildResponse(userId, planActuel, cible, justification, confidence);
    }

    private SubscriptionPlan resolveDefaultPlan() {
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll().stream()
                .sorted(Comparator.comparing(SubscriptionPlan::getPrice))
                .collect(Collectors.toList());

        if (plans.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun plan d'abonnement n'est disponible.");
        }

        return plans.get(0);
    }

    private SubscriptionPlan resolveSimulationTarget(SubscriptionPlan planActuel, List<SubscriptionPlan> orderedPlans, AdjustmentType scenario) {
        if (scenario == AdjustmentType.OPTIMAL) {
            return planActuel;
        }

        int idx = -1;
        for (int i = 0; i < orderedPlans.size(); i++) {
            if (Objects.equals(orderedPlans.get(i).getId(), planActuel.getId())) {
                idx = i;
                break;
            }
        }

        if (idx < 0) {
            return planActuel;
        }

        if (scenario == AdjustmentType.MONTEE) {
            return idx + 1 < orderedPlans.size() ? orderedPlans.get(idx + 1) : planActuel;
        }

        return idx - 1 >= 0 ? orderedPlans.get(idx - 1) : planActuel;
    }

    private RecommendationResponse getRecommendationFromProfile(Long userId, SubscriptionPlan fallbackPlanActuel) {
        PatientProfile profile = patientProfileRepository.findByUserId(userId)
                .orElse(null);

        if (profile == null) {
            SubscriptionPlan planActuel = fallbackPlanActuel;
            return RecommendationResponse.builder()
                    .planId(planActuel != null ? planActuel.getId() : null)
                    .planName(planActuel != null ? planActuel.getName() : null)
                    .planPrice(planActuel != null ? planActuel.getPrice() : null)
                    .confidenceScore(1.0)
                    .justification("Aucun profil patient n'est disponible. Veuillez compléter le Pilier 1 pour obtenir une recommandation médicale personnalisée.")
                    .build();
        }

        PatientProfileRequest req = new PatientProfileRequest();
        req.setUserId(profile.getUserId());
        req.setAgeEnfant(profile.getAgeEnfant());
        req.setMoisDepuisGreffe(profile.getMoisDepuisGreffe());
        req.setComorbidites(profile.getComorbidites());
        req.setFrequenceSuivi(profile.getFrequenceSuivi());
        req.setAEuEpisodeRejet(Boolean.TRUE.equals(profile.getAEuEpisodeRejet()) ? 1 : 0);
        req.setNombreHospitalisationsAn(profile.getNombreHospitalisationsAn());
        req.setPrendImmunosuppresseurs(Boolean.TRUE.equals(profile.getPrendImmunosuppresseurs()) ? 1 : 0);
        req.setNombreMedicamentsQuotidiens(profile.getNombreMedicamentsQuotidiens());
        req.setPresenceComplicationActive(Boolean.TRUE.equals(profile.getPresenceComplicationActive()) ? 1 : 0);

        return recommendationService.recommend(req);
    }

    private AdjustmentProposalResponse buildResponse(Long userId,
                                                     SubscriptionPlan planActuel,
                                                     SubscriptionPlan planRecommande,
                                                     String justification,
                                                     Double confidenceScore) {
        double actuelPrice = planActuel.getPrice() != null ? planActuel.getPrice() : 0.0;
        double recoPrice = planRecommande.getPrice() != null ? planRecommande.getPrice() : 0.0;

        AdjustmentType type;
        if (Objects.equals(planActuel.getId(), planRecommande.getId())) {
            type = AdjustmentType.OPTIMAL;
        } else if (recoPrice > actuelPrice) {
            type = AdjustmentType.MONTEE;
        } else {
            type = AdjustmentType.DESCENTE;
        }

        return AdjustmentProposalResponse.builder()
                .userId(userId)
                .planActuelId(planActuel.getId())
                .planActuelName(planActuel.getName())
                .planActuelPrice(planActuel.getPrice())
                .planRecommandeId(planRecommande.getId())
                .planRecommandeName(planRecommande.getName())
                .planRecommandePrice(planRecommande.getPrice())
                .typeAjustement(type)
                .difference(recoPrice - actuelPrice)
                .justification(justification)
                .confidenceScore(confidenceScore)
                .checkDate(LocalDateTime.now())
                .build();
    }
}
