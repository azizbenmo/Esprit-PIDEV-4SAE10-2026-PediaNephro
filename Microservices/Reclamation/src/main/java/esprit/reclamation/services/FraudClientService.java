package esprit.reclamation.services;

import esprit.reclamation.dto.FraudAnalyzeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class FraudClientService {

    private final RestTemplate restTemplate;
    private final String fraudServiceUrl;

    public FraudClientService(
            @Value("${fraud.service.url:http://localhost:8085}") String fraudServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.fraudServiceUrl = fraudServiceUrl;
    }

    public void analyzeAction(Long userId, String action, String ipAddress, String deviceInfo) {
        try {
            FraudAnalyzeRequest request = new FraudAnalyzeRequest(userId, action, ipAddress, deviceInfo);
            restTemplate.postForObject(fraudServiceUrl + "/fraud/analyze", request, Object.class);
            log.info("Sent fraud analysis from Reclamation for user: {} action: {}", userId, action);
        } catch (Exception e) {
            log.error("Failed to call Fraud Service from Reclamation: {}", e.getMessage());
        }
    }
}
