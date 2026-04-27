package com.pedianephro.subscription.service;

import com.pedianephro.subscription.dto.*;
import com.pedianephro.subscription.entity.RiskScoreHistory;
import com.pedianephro.subscription.entity.UserBehavior;
import com.pedianephro.subscription.entity.Subscription;
import com.pedianephro.subscription.entity.SubscriptionStatus;
import com.pedianephro.subscription.repository.RiskScoreHistoryRepository;
import com.pedianephro.subscription.repository.UserBehaviorRepository;
import com.pedianephro.subscription.repository.SubscriptionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskScoreService {

    private final UserBehaviorRepository userBehaviorRepository;
    private final RiskScoreHistoryRepository riskScoreHistoryRepository;
    private final SubscriptionRepository subscriptionRepository;
    
    private Classifier mlModel;
    private Instances datasetStructure;

    private static final String RISK_LOW = "FAIBLE";
    private static final String RISK_MEDIUM = "MOYEN";
    private static final String RISK_HIGH = "ÉLEVÉ";

    @PostConstruct
    public void init() {
        try {
            ClassPathResource modelFile = new ClassPathResource("ml/risk_model.model");
            if (modelFile.exists()) {
                this.mlModel = (Classifier) SerializationHelper.read(modelFile.getInputStream());
                initializeDatasetStructure();
                log.info("Modèle Weka Risk chargé avec succès depuis {}", modelFile.getPath());
            } else {
                log.warn("Fichier modèle ML Risk non trouvé à 'src/main/resources/ml/risk_model.model'.");
            }
        } catch (Exception e) {
            log.error("Erreur lors du chargement du modèle Weka Risk: {}", e.getMessage());
        }
    }

    private void initializeDatasetStructure() {
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("jours_sans_connexion"));
        attributes.add(new Attribute("bilans_en_retard"));
        attributes.add(new Attribute("rappels_ignores"));
        attributes.add(new Attribute("rendez_vous_annules"));
        attributes.add(new Attribute("medicaments_non_confirmes"));
        
        ArrayList<String> classValues = new ArrayList<>();
        classValues.add(RISK_LOW);
        classValues.add(RISK_MEDIUM);
        classValues.add(RISK_HIGH);
        attributes.add(new Attribute("risk_level", classValues));

        datasetStructure = new Instances("RiskRelation", attributes, 0);
        datasetStructure.setClassIndex(datasetStructure.numAttributes() - 1);
    }

    @Transactional
    public RiskScoreResponse calculateRiskScore(Long userId) {
        // Chercher l'abonnement actif pour lier au comportement
        Optional<Subscription> activeSub = subscriptionRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(userId) && SubscriptionStatus.ACTIVE.equals(s.getStatus()))
                .findFirst();

        UserBehavior behavior = userBehaviorRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserBehavior newBehavior = UserBehavior.builder()
                            .userId(userId)
                            .joursSansConnexion(0)
                            .bilansEnRetard(0)
                            .rappelsIgnores(0)
                            .rendezVousAnnules(0)
                            .medicamentsNonConfirmes(0)
                            .build();
                    return userBehaviorRepository.save(newBehavior);
                });

        // Mise à jour de l'ID d'abonnement si trouvé et non encore lié
        if (activeSub.isPresent() && behavior.getSubscriptionId() == null) {
            behavior.setSubscriptionId(activeSub.get().getId());
            userBehaviorRepository.save(behavior);
        }

        // 1. Calcul via Formule Java (Règles Métier)
        double scoreBase = (behavior.getJoursSansConnexion() * 2.5)
                + (behavior.getBilansEnRetard() * 20.0)
                + (behavior.getRappelsIgnores() * 8.0)
                + (behavior.getRendezVousAnnules() * 12.0)
                + (behavior.getMedicamentsNonConfirmes() * 15.0);
        
        double scoreFinal = Math.min(scoreBase, 100.0);
        String riskLevelRule = determineRiskLevel(scoreFinal);

        // 2. Validation via Modèle ML
        String riskLevelML = predictML(behavior);
        
        // 3. Fusion : Si concordance, confiance totale. Sinon, les règles métier priment.
        String finalRiskLevel = riskLevelRule;
        if (riskLevelML != null && riskLevelML.equals(riskLevelRule)) {
            log.info("Concordance Règles/ML pour l'utilisateur {}: {}", userId, finalRiskLevel);
        } else if (riskLevelML != null) {
            log.info("Discordance Règles ({}) / ML ({}) pour l'utilisateur {}. Règles prioritaires.", riskLevelRule, riskLevelML, userId);
        }

        String action = determineAction(finalRiskLevel);

        // 4. Sauvegarde de l'historique
        RiskScoreHistory history = RiskScoreHistory.builder()
                .userId(userId)
                .score(scoreFinal)
                .riskLevel(finalRiskLevel)
                .actionRecommandee(action)
                .build();
        riskScoreHistoryRepository.save(history);

        // 5. Récupération de l'historique récent
        List<RiskScoreHistoryDto> recentHistory = riskScoreHistoryRepository.findTop30ByUserIdOrderByCalculatedAtDesc(userId)
                .stream()
                .map(h -> RiskScoreHistoryDto.builder()
                        .score(h.getScore())
                        .riskLevel(h.getRiskLevel())
                        .calculatedAt(h.getCalculatedAt())
                        .build())
                .collect(Collectors.toList());

        return RiskScoreResponse.builder()
                .userId(userId)
                .score(scoreFinal)
                .riskLevel(finalRiskLevel)
                .actionRecommandee(action)
                .calculatedAt(history.getCalculatedAt() != null ? history.getCalculatedAt() : LocalDateTime.now())
                .scoreHistory(recentHistory)
                .build();
    }

    private String determineRiskLevel(double score) {
        if (score < 30) return RISK_LOW;
        if (score < 70) return RISK_MEDIUM;
        return RISK_HIGH;
    }

    private String determineAction(String riskLevel) {
        switch (riskLevel) {
            case RISK_LOW: return "Famille engagée, aucune intervention requise.";
            case RISK_MEDIUM: return "Envoyer un email de rappel personnalisé à la famille.";
            case RISK_HIGH: return "Alerte urgente au néphrologue + contact téléphonique recommandé.";
            default: return "Surveillance standard.";
        }
    }

    private String predictML(UserBehavior behavior) {
        if (mlModel == null) return null;
        try {
            DenseInstance instance = new DenseInstance(datasetStructure.numAttributes());
            instance.setDataset(datasetStructure);
            instance.setValue(0, behavior.getJoursSansConnexion());
            instance.setValue(1, behavior.getBilansEnRetard());
            instance.setValue(2, behavior.getRappelsIgnores());
            instance.setValue(3, behavior.getRendezVousAnnules());
            instance.setValue(4, behavior.getMedicamentsNonConfirmes());

            double result = mlModel.classifyInstance(instance);
            return datasetStructure.classAttribute().value((int) result);
        } catch (Exception e) {
            log.error("Erreur prédiction ML Risk: {}", e.getMessage());
            return null;
        }
    }

    @Transactional
    public void updateBehavior(UserBehaviorRequest request) {
        UserBehavior behavior = userBehaviorRepository.findByUserId(request.getUserId())
                .orElse(new UserBehavior());
        
        behavior.setUserId(request.getUserId());
        behavior.setJoursSansConnexion(request.getJoursSansConnexion());
        behavior.setBilansEnRetard(request.getBilansEnRetard());
        behavior.setRappelsIgnores(request.getRappelsIgnores());
        behavior.setRendezVousAnnules(request.getRendezVousAnnules());
        behavior.setMedicamentsNonConfirmes(request.getMedicamentsNonConfirmes());
        
        userBehaviorRepository.save(behavior);
    }

    public void calculateAllActiveSubscriptions() {
        List<Subscription> actives = subscriptionRepository.findAll().stream()
                .filter(s -> SubscriptionStatus.ACTIVE.equals(s.getStatus()))
                .collect(Collectors.toList());
        
        log.info("Lancement du calcul de risque pour {} abonnements actifs...", actives.size());
        for (Subscription sub : actives) {
            try {
                calculateRiskScore(sub.getUserId());
            } catch (Exception e) {
                log.error("Erreur calcul risque pour utilisateur {}: {}", sub.getUserId(), e.getMessage());
            }
        }
    }

    public List<HighRiskPatientDto> getHighRiskPatients() {
        return riskScoreHistoryRepository.findByRiskLevel(RISK_HIGH).stream()
                .collect(Collectors.groupingBy(RiskScoreHistory::getUserId))
                .values().stream()
                .map(list -> list.get(0)) // Prendre le plus récent par utilisateur si possible, ou filtrer par date
                .map(h -> HighRiskPatientDto.builder()
                        .userId(h.getUserId())
                        .score(h.getScore())
                        .riskLevel(h.getRiskLevel())
                        .actionRecommandee(h.getActionRecommandee())
                        // Note: userEmail et userFullName devraient être récupérés via un service User/Auth
                        .userEmail("patient" + h.getUserId() + "@example.com") 
                        .userFullName("Patient " + h.getUserId())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public RiskScoreResponse simulateBehavior(Long userId) {
        Random rand = new Random();
        UserBehaviorRequest request = UserBehaviorRequest.builder()
                .userId(userId)
                .joursSansConnexion(rand.nextInt(15))
                .bilansEnRetard(rand.nextInt(3))
                .rappelsIgnores(rand.nextInt(5))
                .rendezVousAnnules(rand.nextInt(2))
                .medicamentsNonConfirmes(rand.nextInt(4))
                .build();
        
        updateBehavior(request);
        return calculateRiskScore(userId);
    }
}
