package esprit.fraud.controller;

import esprit.fraud.dto.FraudAnalyzeRequest;
import esprit.fraud.entities.FraudEvent;
import esprit.fraud.repositories.FraudEventRepository;
import esprit.fraud.services.FraudDetectionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/fraud")
public class FraudController {

    private final FraudDetectionService fraudDetectionService;
    private final FraudEventRepository fraudEventRepository;

    public FraudController(FraudDetectionService fraudDetectionService, FraudEventRepository fraudEventRepository) {
        this.fraudDetectionService = fraudDetectionService;
        this.fraudEventRepository = fraudEventRepository;
    }

    @PostMapping("/analyze")
    public ResponseEntity<FraudEvent> analyzeAction(@RequestBody @Valid FraudAnalyzeRequest request) {
        log.info("Requete d'analyse recue pour Action: {}", request.getAction());
        FraudEvent event = fraudDetectionService.analyzeAndSave(request);
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<List<FraudEvent>> getAllEvents() {
        return ResponseEntity.ok(fraudEventRepository.findAll());
    }

    @GetMapping("/suspicious")
    public ResponseEntity<List<FraudEvent>> getSuspiciousEvents() {
        return ResponseEntity.ok(fraudEventRepository.findBySuspiciousTrueOrderByCreatedAtDesc());
    }
}
