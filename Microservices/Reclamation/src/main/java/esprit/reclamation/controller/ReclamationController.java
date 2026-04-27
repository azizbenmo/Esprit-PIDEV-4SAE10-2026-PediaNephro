package esprit.reclamation.controller;

import esprit.reclamation.dto.CsatEvaluationResponseDTO;
import esprit.reclamation.dto.CsatEvaluationStatusDTO;
import esprit.reclamation.dto.CsatStatistiquesDTO;
import esprit.reclamation.dto.CsatSubmitDTO;
import esprit.reclamation.dto.ReclamationHistoryDTO;
import esprit.reclamation.dto.ReclamationRequestDTO;
import esprit.reclamation.dto.ReclamationResponseDTO;
import esprit.reclamation.dto.ReponseAdminDTO;
import esprit.reclamation.dto.StatistiquesDTO;
import esprit.reclamation.entities.StatutReclamation;
import esprit.reclamation.security.audit.LogAction;
import esprit.reclamation.services.FraudClientService;
import esprit.reclamation.services.ReclamationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CORS : appels directs depuis Angular dev (localhost:4200 → 127.0.0.1:8086), comme Postman. */
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, maxAge = 3600)
@RestController
@RequestMapping("/api/reclamations")
@RequiredArgsConstructor
public class ReclamationController {

    @Value("${global.message:Message local par defaut (reclamation)})")
    private String configMessage;

    private final ReclamationService reclamationService;
    private final FraudClientService fraudClientService;

    @GetMapping("/config-message")
    public ResponseEntity<String> getConfigMessage() {
        return ResponseEntity.ok(configMessage);
    }

    @PostMapping
    @LogAction(action = "CREATE_RECLAMATION")
    public ResponseEntity<ReclamationResponseDTO> creer(
            @Valid @RequestBody ReclamationRequestDTO dto, HttpServletRequest request) {
        ReclamationResponseDTO response = reclamationService.creer(dto);
        try {
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getRemoteAddr();
            }
            String userAgent = request.getHeader("User-Agent");
            fraudClientService.analyzeAction(
                    dto.getUserId(), "RECLAMATION_CREATE", ipAddress, userAgent != null ? userAgent : "Unknown");
        } catch (Throwable t) {
            // ne pas faire échouer la création si fraude / proxy indisponible
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReclamationResponseDTO>> getAll() {
        return ResponseEntity.ok(reclamationService.getAll());
    }

    @GetMapping("/statistiques")
    public ResponseEntity<StatistiquesDTO> statistiques() {
        return ResponseEntity.ok(reclamationService.getStatistiques());
    }

    @GetMapping("/statistiques/csat")
    public ResponseEntity<CsatStatistiquesDTO> statistiquesCsat() {
        return ResponseEntity.ok(reclamationService.getCsatStatistiques());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReclamationResponseDTO>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(reclamationService.getByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReclamationResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reclamationService.getById(id));
    }

    @PutMapping("/{id}/repondre")
    @LogAction(action = "RESPOND_RECLAMATION")
    public ResponseEntity<ReclamationResponseDTO> repondre(
            @PathVariable Long id, @Valid @RequestBody ReponseAdminDTO dto) {
        return ResponseEntity.ok(reclamationService.repondre(id, dto));
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<ReclamationResponseDTO> changerStatut(
            @PathVariable Long id, @RequestParam StatutReclamation statut) {
        return ResponseEntity.ok(reclamationService.changerStatut(id, statut));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        reclamationService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<ReclamationHistoryDTO>> timeline(@PathVariable Long id) {
        return ResponseEntity.ok(reclamationService.getTimeline(id));
    }

    @GetMapping("/{id}/evaluation")
    public ResponseEntity<CsatEvaluationStatusDTO> getCsatStatus(@PathVariable Long id) {
        return ResponseEntity.ok(reclamationService.getCsatStatus(id));
    }

    @PostMapping("/{id}/evaluation")
    public ResponseEntity<CsatEvaluationResponseDTO> soumettreEvaluation(
            @PathVariable Long id, @Valid @RequestBody CsatSubmitDTO dto) {
        CsatEvaluationResponseDTO body = reclamationService.soumettreEvaluation(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
