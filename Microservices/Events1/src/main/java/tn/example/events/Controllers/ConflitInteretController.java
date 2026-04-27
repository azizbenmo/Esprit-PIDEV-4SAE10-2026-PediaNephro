package tn.example.events.Controllers;



import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.example.events.Entities.StatutPartenariat;
import tn.example.events.dto.ConflitAnalyseResult;
import tn.example.events.Entities.Partenariat;
import tn.example.events.Services.ConflitInteretService;
import tn.example.events.Services.PartenariatService;

@RestController
@RequestMapping("/partenariats/conflit-interet")
@RequiredArgsConstructor
public class ConflitInteretController {

    private final ConflitInteretService conflitInteretService;
    private final PartenariatService partenariatService;

    /**
     * Analyse un partenariat existant par son ID
     */
    @GetMapping("/analyser/{id}")
    public ResponseEntity<ConflitAnalyseResult> analyserParId(@PathVariable Long id) {
        Partenariat partenariat = partenariatService.findById(id);
        ConflitAnalyseResult result = conflitInteretService.analyserConflitInteret(partenariat);
        return ResponseEntity.ok(result);
    }

    /**
     * Analyse + approuve automatiquement si pas de conflit critique
     */
    @PostMapping("/analyser-et-decider/{id}")
    public ResponseEntity<ConflitAnalyseResult> analyserEtDecider(@PathVariable Long id) {
        Partenariat partenariat = partenariatService.findById(id);
        ConflitAnalyseResult result = conflitInteretService.analyserConflitInteret(partenariat);

        // Auto-refus si CRITIQUE
        if ("CRITIQUE".equals(result.getNiveauRisque()) || "REFUSER".equals(result.getRecommandation())) {
            partenariatService.updateStatut(id, StatutPartenariat.valueOf("REFUSE"));
        }

        return ResponseEntity.ok(result);
    }
}
