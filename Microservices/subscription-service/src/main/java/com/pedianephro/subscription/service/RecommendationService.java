package com.pedianephro.subscription.service;

import com.pedianephro.subscription.dto.ConseilMedical;
import com.pedianephro.subscription.dto.PatientProfileRequest;
import com.pedianephro.subscription.dto.PlanScore;
import com.pedianephro.subscription.dto.RecommendationResponse;
import com.pedianephro.subscription.entity.PatientProfile;
import com.pedianephro.subscription.entity.SubscriptionPlan;
import com.pedianephro.subscription.repository.PatientProfileRepository;
import com.pedianephro.subscription.repository.SubscriptionPlanRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final PatientProfileRepository patientProfileRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private Classifier mlModel;
    private Instances datasetStructure;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource modelFile = new ClassPathResource("ml/subscription_model.model");
            if (modelFile.exists()) {
                this.mlModel = (Classifier) SerializationHelper.read(modelFile.getInputStream());
                initializeDatasetStructure();
                log.info("Modèle Weka chargé avec succès depuis {}", modelFile.getPath());
            } else {
                log.warn("Fichier modèle ML non trouvé à 'src/main/resources/ml/subscription_model.model'. La recommandation utilisera uniquement les règles métier.");
            }
        } catch (Exception e) {
            log.error("Erreur lors du chargement du modèle Weka: {}", e.getMessage());
        }
    }

    private void initializeDatasetStructure() {
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("age_enfant"));
        attributes.add(new Attribute("mois_depuis_greffe"));
        attributes.add(new Attribute("a_eu_episode_rejet"));
        attributes.add(new Attribute("nombre_hospitalisations_an"));
        attributes.add(new Attribute("prend_immunosuppresseurs"));
        attributes.add(new Attribute("nombre_medicaments_quotidiens"));
        attributes.add(new Attribute("presence_complication_active"));
        
        ArrayList<String> classValues = new ArrayList<>();
        classValues.add("1"); // Basique
        classValues.add("2"); // Premium
        classValues.add("3"); // Pro
        attributes.add(new Attribute("plan_id", classValues));

        datasetStructure = new Instances("RecommendationRelation", attributes, 0);
        datasetStructure.setClassIndex(datasetStructure.numAttributes() - 1);
    }

    public RecommendationResponse recommend(PatientProfileRequest request) {
        // 1. Règles métier (Expert)
        Long rulePlanId = applyBusinessRules(request);
        
        // 2. Modèle ML
        PlanScore mlPrediction = getMlPrediction(request);
        
        // 3. Fusion et Justification
        return finalizeRecommendation(request, rulePlanId, mlPrediction);
    }

    @Transactional
    public RecommendationResponse saveProfileAndRecommend(PatientProfileRequest request) {
        Optional<PatientProfile> existing = patientProfileRepository.findByUserId(request.getUserId());
        PatientProfile profile = existing.orElse(new PatientProfile());
        
        profile.setUserId(request.getUserId());
        profile.setAgeEnfant(request.getAgeEnfant());
        profile.setMoisDepuisGreffe(request.getMoisDepuisGreffe());
        profile.setComorbidites(request.getComorbidites() != null ? request.getComorbidites() : (profile.getComorbidites() != null ? profile.getComorbidites() : 0));
        profile.setFrequenceSuivi(request.getFrequenceSuivi() != null ? request.getFrequenceSuivi() : (profile.getFrequenceSuivi() != null ? profile.getFrequenceSuivi() : 1));
        profile.setAEuEpisodeRejet(request.getAEuEpisodeRejet() != null && request.getAEuEpisodeRejet() == 1);
        profile.setNombreHospitalisationsAn(request.getNombreHospitalisationsAn());
        profile.setPrendImmunosuppresseurs(request.getPrendImmunosuppresseurs() != null && request.getPrendImmunosuppresseurs() == 1);
        profile.setNombreMedicamentsQuotidiens(request.getNombreMedicamentsQuotidiens());
        profile.setPresenceComplicationActive(request.getPresenceComplicationActive() != null && request.getPresenceComplicationActive() == 1);
        
        patientProfileRepository.save(profile);
        return recommend(request);
    }

    private Long applyBusinessRules(PatientProfileRequest request) {
        int mois = request.getMoisDepuisGreffe();
        int age = request.getAgeEnfant();
        int rejet = request.getAEuEpisodeRejet() != null ? request.getAEuEpisodeRejet() : 0;
        int hospit = request.getNombreHospitalisationsAn() != null ? request.getNombreHospitalisationsAn() : 0;
        int immuno = request.getPrendImmunosuppresseurs() != null ? request.getPrendImmunosuppresseurs() : 1;
        int meds = request.getNombreMedicamentsQuotidiens() != null ? request.getNombreMedicamentsQuotidiens() : 1;
        int comp = request.getPresenceComplicationActive() != null ? request.getPresenceComplicationActive() : 0;

        if (mois < 6 || rejet == 1 || hospit >= 3 || immuno == 0 || comp == 1) {
            return 3L; // PRO
        } else if ((mois >= 6 && mois <= 24) || meds >= 4 || (hospit >= 1 && hospit < 3) || age >= 12) {
            return 2L; // PREMIUM
        } else {
            return 1L; // BASIQUE
        }
    }

    private PlanScore getMlPrediction(PatientProfileRequest request) {
        if (mlModel == null) return null;

        try {
            DenseInstance instance = new DenseInstance(datasetStructure.numAttributes());
            instance.setDataset(datasetStructure);
            instance.setValue(0, request.getAgeEnfant());
            instance.setValue(1, request.getMoisDepuisGreffe());
            instance.setValue(2, request.getAEuEpisodeRejet() != null ? request.getAEuEpisodeRejet() : 0);
            instance.setValue(3, request.getNombreHospitalisationsAn() != null ? request.getNombreHospitalisationsAn() : 0);
            instance.setValue(4, request.getPrendImmunosuppresseurs() != null ? request.getPrendImmunosuppresseurs() : 1);
            instance.setValue(5, request.getNombreMedicamentsQuotidiens() != null ? request.getNombreMedicamentsQuotidiens() : 1);
            instance.setValue(6, request.getPresenceComplicationActive() != null ? request.getPresenceComplicationActive() : 0);

            double result = mlModel.classifyInstance(instance);
            double[] distributions = mlModel.distributionForInstance(instance);
            
            String predictedIdStr = datasetStructure.classAttribute().value((int) result);
            Long predictedId = Long.parseLong(predictedIdStr);
            double confidence = distributions[(int) result] * 100;

            return new PlanScore(predictedId, "", confidence);
        } catch (Exception e) {
            log.error("Erreur lors de la prédiction ML: {}", e.getMessage());
            return null;
        }
    }

    private RecommendationResponse finalizeRecommendation(PatientProfileRequest request, Long ruleId, PlanScore mlScore) {
        Long finalPlanId = ruleId; // Les règles métier priment par défaut
        double confidence = 100.0;
        
        if (mlScore != null) {
            if (mlScore.getPlanId().equals(ruleId)) {
                // Concordance -> On garde 100% ou on booste si < 100
                confidence = Math.max(95.0, mlScore.getScore());
            } else {
                // Discordance -> Les règles priment, on baisse la confiance
                confidence = 70.0;
            }
        }

        SubscriptionPlan plan = resolvePlanForRecommendation(finalPlanId);
        List<PlanScore> allRanked = generateAllScores(ruleId, mlScore);

        return RecommendationResponse.builder()
                .planId(plan.getId())
                .planName(plan.getName())
                .planPrice(plan.getPrice())
                .confidenceScore(confidence)
                .justification(generateJustification(request, plan.getName()))
                .allPlansRanked(allRanked)
                .conseilsMedicaux(generateConseilsMedicaux(request))
                .alertesMedicales(generateAlertesMedicales(request))
                .build();
    }

    /**
     * Les règles / le ML utilisent des IDs logiques 1–3. Si la base a d’autres IDs
     * (ou pas encore de seed), on retombe sur le nom du plan puis sur l’ordre des prix.
     */
    private SubscriptionPlan resolvePlanForRecommendation(Long logicalPlanId) {
        if (logicalPlanId == null) {
            logicalPlanId = 1L;
        }
        Optional<SubscriptionPlan> byId = subscriptionPlanRepository.findById(logicalPlanId);
        if (byId.isPresent()) {
            return byId.get();
        }
        String nameMatch = switch (logicalPlanId.intValue()) {
            case 1 -> "Basique";
            case 2 -> "Premium";
            case 3 -> "Pro";
            default -> null;
        };
        if (nameMatch != null) {
            Optional<SubscriptionPlan> byName = subscriptionPlanRepository.findAll().stream()
                    .filter(p -> nameMatch.equalsIgnoreCase(p.getName()))
                    .findFirst();
            if (byName.isPresent()) {
                log.warn("Plan id {} absent en base — utilisation du plan « {} ».", logicalPlanId, byName.get().getName());
                return byName.get();
            }
        }
        List<SubscriptionPlan> byPrice = subscriptionPlanRepository.findAll().stream()
                .sorted(Comparator.comparing(SubscriptionPlan::getPrice))
                .toList();
        if (byPrice.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Aucun plan d'abonnement configuré.");
        }
        int idx = Math.min(Math.max(logicalPlanId.intValue() - 1, 0), byPrice.size() - 1);
        SubscriptionPlan fallback = byPrice.get(idx);
        log.warn("Plan id {} absent — utilisation du plan à l'index {} ({}).", logicalPlanId, idx, fallback.getName());
        return fallback;
    }

    private List<PlanScore> generateAllScores(Long ruleId, PlanScore mlScore) {
        List<PlanScore> scores = new ArrayList<>();
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();

        for (SubscriptionPlan p : plans) {
            double s = 0.0;
            if (p.getId().equals(ruleId)) s += 60.0;
            if (mlScore != null && p.getId().equals(mlScore.getPlanId())) s += mlScore.getScore() * 0.4;
            
            // Normalisation basique
            if (s == 0) s = 5.0 + (Math.random() * 5); // Score résiduel pour les autres
            
            scores.add(new PlanScore(p.getId(), p.getName(), Math.min(100.0, s)));
        }

        scores.sort(Comparator.comparing(PlanScore::getScore).reversed());
        return scores;
    }

    private String generateJustification(PatientProfileRequest request, String recommendedPlan) {
        StringBuilder sb = new StringBuilder();
        int mois = request.getMoisDepuisGreffe();
        
        if (mois < 6) {
            sb.append("La greffe est très récente (moins de 6 mois). ");
        } else if (mois <= 24) {
            sb.append("Votre enfant est dans une phase de suivi intermédiaire post-greffe. ");
        } else {
            sb.append("La greffe est stabilisée depuis plus de 2 ans. ");
        }

        int rejet = request.getAEuEpisodeRejet() != null ? request.getAEuEpisodeRejet() : 0;
        int comp = request.getPresenceComplicationActive() != null ? request.getPresenceComplicationActive() : 0;
        int hospit = request.getNombreHospitalisationsAn() != null ? request.getNombreHospitalisationsAn() : 0;
        
        if (rejet == 1) {
            sb.append("L'historique de rejet nécessite une surveillance accrue. ");
        }
        if (comp == 1) {
            sb.append("Une complication active requiert un suivi médical étroit. ");
        }
        if (hospit >= 3) {
            sb.append("Les hospitalisations fréquentes justifient un encadrement renforcé. ");
        }

        sb.append("Le plan ").append(recommendedPlan).append(" est le plus adapté pour garantir un suivi médical sécurisé et complet selon ce profil.");
        
        return sb.toString();
    }

    private List<ConseilMedical> generateConseilsMedicaux(PatientProfileRequest request) {
        List<ConseilMedical> conseils = new ArrayList<>();
        
        int immuno = request.getPrendImmunosuppresseurs() != null ? request.getPrendImmunosuppresseurs() : 1;
        int rejet = request.getAEuEpisodeRejet() != null ? request.getAEuEpisodeRejet() : 0;
        int mois = request.getMoisDepuisGreffe() != null ? request.getMoisDepuisGreffe() : 0;
        int comp = request.getPresenceComplicationActive() != null ? request.getPresenceComplicationActive() : 0;
        int hospit = request.getNombreHospitalisationsAn() != null ? request.getNombreHospitalisationsAn() : 0;
        int meds = request.getNombreMedicamentsQuotidiens() != null ? request.getNombreMedicamentsQuotidiens() : 1;
        int age = request.getAgeEnfant() != null ? request.getAgeEnfant() : 0;

        if (immuno == 0) {
            conseils.add(new ConseilMedical("IMMUNOSUPPRESSEURS", 
                "ATTENTION : L'arrêt des immunosuppresseurs sans avis médical peut provoquer un rejet aigu du greffon. Contactez immédiatement le néphrologue.", 
                "CRITIQUE"));
        }
        
        if (rejet == 1) {
            conseils.add(new ConseilMedical("REJET", 
                "Un épisode de rejet antérieur a été détecté. Surveillez attentivement : fièvre > 38°C, douleur au niveau du greffon, baisse soudaine de la production d'urine. Consultez en urgence si l'un de ces signes apparaît.", 
                "HAUTE"));
        }
        
        if (mois < 12) {
            conseils.add(new ConseilMedical("SUIVI_BIOLOGIQUE", 
                "Votre enfant est dans la première année post-greffe. Les bilans sanguins doivent être effectués toutes les 2 semaines sans exception pour détecter tout signe de rejet précoce.", 
                "HAUTE"));
        }
        
        if (comp == 1) {
            conseils.add(new ConseilMedical("COMPLICATIONS", 
                "Une complication active est en cours. Contrôlez la tension artérielle quotidiennement et respectez strictement le traitement prescrit par le néphrologue.", 
                "HAUTE"));
        }
        
        if (hospit >= 3) {
            conseils.add(new ConseilMedical("HOSPITALISATIONS", 
                "Le nombre élevé d'hospitalisations cette année indique un cas complexe. Ne manquez aucun rendez-vous de suivi et contactez le néphrologue au moindre doute.", 
                "HAUTE"));
        }
        
        if (meds >= 4) {
            String urgence = meds >= 7 ? "HAUTE" : "MOYENNE";
            conseils.add(new ConseilMedical("MEDICAMENTS", 
                "Avec " + meds + " médicaments quotidiens, utilisez un pilulier hebdomadaire et cochez chaque prise dans l'application pour éviter les oublis dangereux.", 
                urgence));
        }
        
        if (age >= 12) {
            conseils.add(new ConseilMedical("ADOLESCENT", 
                "À l'adolescence, certains patients oublient ou refusent de prendre leurs médicaments immunosuppresseurs. Maintenez un dialogue ouvert avec votre enfant sur l'importance vitale du traitement.", 
                "MOYENNE"));
        }
        
        if (mois > 24 && rejet == 0 && comp == 0) {
            conseils.add(new ConseilMedical("STABILITE", 
                "Votre enfant montre une belle stabilité post-greffe. Continuez le suivi régulier et les bilans périodiques pour maintenir cette évolution positive.", 
                "NORMALE"));
        }
        
        return conseils;
    }

    private List<String> generateAlertesMedicales(PatientProfileRequest request) {
        List<String> alertes = new ArrayList<>();
        
        int immuno = request.getPrendImmunosuppresseurs() != null ? request.getPrendImmunosuppresseurs() : 1;
        int rejet = request.getAEuEpisodeRejet() != null ? request.getAEuEpisodeRejet() : 0;
        int hospit = request.getNombreHospitalisationsAn() != null ? request.getNombreHospitalisationsAn() : 0;
        int comp = request.getPresenceComplicationActive() != null ? request.getPresenceComplicationActive() : 0;
        int mois = request.getMoisDepuisGreffe() != null ? request.getMoisDepuisGreffe() : 0;

        if (immuno == 0) {
            alertes.add("ALERTE CRITIQUE : Arrêt des immunosuppresseurs détecté");
        }
        if (rejet == 1) {
            alertes.add("Épisode de rejet antérieur détecté — surveillance renforcée permanente requise");
        }
        if (hospit >= 3) {
            alertes.add("Nombre élevé d'hospitalisations : " + hospit + " cette année");
        }
        if (comp == 1) {
            alertes.add("Complication active en cours — suivi intensif requis");
        }
        if (mois < 6) {
            alertes.add("Greffe très récente (" + mois + " mois) — phase critique en cours");
        }
        
        return alertes;
    }
}
