package esprit.fraud.services;

import esprit.fraud.dto.FraudAnalyzeRequest;
import esprit.fraud.entities.FraudEvent;
import esprit.fraud.repositories.FraudEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class FraudDetectionService {

    private final FraudEventRepository fraudEventRepository;
    private final WebClient webClient;
    private final EmailService emailService;
    private final String aiServiceUrl;

    public FraudDetectionService(
            FraudEventRepository fraudEventRepository,
            WebClient webClient,
            EmailService emailService,
            @Value("${ai.service.url:http://localhost:8000}") String aiServiceUrl) {
        this.fraudEventRepository = fraudEventRepository;
        this.webClient = webClient;
        this.emailService = emailService;
        this.aiServiceUrl = aiServiceUrl;
    }

    public FraudEvent analyzeAndSave(FraudAnalyzeRequest request) {
        log.info("Analyse de fraude demarree pour Action: {}, User: {}", request.getAction(), request.getUserId());

        // Prepare request payload for AI Service
        Map<String, Object> aiRequest = new HashMap<>();
        aiRequest.put("user_id", request.getUserId());
        aiRequest.put("action", request.getAction());
        aiRequest.put("ip_address", request.getIpAddress());
        aiRequest.put("device_info", request.getDeviceInfo());
        aiRequest.put("timestamp", LocalDateTime.now().toString());

        double score;
        boolean suspicious;
        String details;

        try {
            // Call AI Service synchronously for now (can be made fully reactive later)
            Map response = webClient.post()
                    .uri(aiServiceUrl + "/predict")
                    .bodyValue(aiRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(); // Blocking call for simplicity in this flow, assuming AI is fast enough

            if (response != null) {
                score = ((Number) response.getOrDefault("score", 0.0)).doubleValue();
                suspicious = (boolean) response.getOrDefault("suspicious", false);
                details = (String) response.getOrDefault("details", "AI Prediction success");
            } else {
                throw new RuntimeException("Reponse vide de l'AI Service");
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'appel a l'AI Service: {}", e.getMessage(), e);
            // Fallback rules if AI Service is down
            score = calculateFallbackScore(request);
            suspicious = score > 70.0;
            details = "Fallback effectue suite a erreur AI: " + e.getMessage();
        }

        FraudEvent event = FraudEvent.builder()
                .userId(request.getUserId())
                .action(request.getAction())
                .ipAddress(request.getIpAddress())
                .deviceInfo(request.getDeviceInfo())
                .score(score)
                .suspicious(suspicious)
                .details(details)
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Detection de fraude resultat: Score: {}, Suspect: {}", score, suspicious);

        // Sauvegarder l'événement en base
        FraudEvent savedEvent = fraudEventRepository.save(event);

        // ✅ Envoyer un email d'alerte si le score dépasse ou atteint 50%
        if (score >= 50.0) {
            log.warn("ALERTE DE FRAUDE! Score = {} (>= 50%) pour action {} par le User ID {}",
                    score, request.getAction(), request.getUserId());
            emailService.sendFraudAlertEmail(request.getEmail(), savedEvent);
        }

        // Alerte critique supplémentaire pour score > 80
        if (score > 80.0) {
            log.warn("ALERTE CRITIQUE DE FRAUDE! Score = {} pour action {} par le User ID {}",
                    score, request.getAction(), request.getUserId());
        }

        return savedEvent;
    }

    private double calculateFallbackScore(FraudAnalyzeRequest request) {
        double currentScore = 0.0;
        if (request.getIpAddress() == null || request.getIpAddress().isBlank()) {
            currentScore += 20.0;
        }
        if ("RECLAMATION_CREATE".equalsIgnoreCase(request.getAction())) {
            currentScore += 30.0;
        }
        return currentScore;
    }
}
