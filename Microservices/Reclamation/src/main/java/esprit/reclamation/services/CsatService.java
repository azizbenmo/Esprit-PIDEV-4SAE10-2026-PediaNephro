package esprit.reclamation.services;

import esprit.reclamation.dto.CsatEvaluationResponseDTO;
import esprit.reclamation.dto.CsatEvaluationStatusDTO;
import esprit.reclamation.dto.CsatStatistiquesDTO;
import esprit.reclamation.dto.CsatSubmitDTO;
import esprit.reclamation.entities.CsatEvaluation;
import esprit.reclamation.entities.Reclamation;
import esprit.reclamation.entities.StatutReclamation;
import esprit.reclamation.exceptions.DuplicateCsatException;
import esprit.reclamation.exceptions.IllegalReclamationStateException;
import esprit.reclamation.exceptions.ReclamationNotFoundException;
import esprit.reclamation.repositories.CsatEvaluationRepository;
import esprit.reclamation.repositories.ReclamationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsatService {

    private final CsatEvaluationRepository csatEvaluationRepository;
    private final ReclamationRepository reclamationRepository;

    @Transactional
    public CsatEvaluationResponseDTO soumettre(Long reclamationId, CsatSubmitDTO dto) {
        Reclamation rec =
                reclamationRepository
                        .findById(reclamationId)
                        .orElseThrow(() -> new ReclamationNotFoundException("Réclamation introuvable (id=" + reclamationId + ")."));

        if (!estEligibleCsat(rec.getStatut())) {
            throw new IllegalReclamationStateException(
                    "Évaluation impossible : statut doit être CLOTUREE ou RESOLUE (réclamation id=" + reclamationId + ").");
        }
        if (csatEvaluationRepository.findByReclamationId(reclamationId).isPresent()) {
            throw new DuplicateCsatException("Une évaluation existe déjà pour cette réclamation.");
        }

        CsatEvaluation eval =
                CsatEvaluation.builder()
                        .reclamationId(reclamationId)
                        .userId(dto.getUserId())
                        .note(dto.getNote())
                        .commentaire(dto.getCommentaire())
                        .build();
        CsatEvaluation saved = csatEvaluationRepository.save(eval);
        log.info("CSAT enregistrée reclamationId={} note={}", reclamationId, dto.getNote());
        return versDto(saved);
    }

    private static boolean estEligibleCsat(StatutReclamation statut) {
        return statut == StatutReclamation.CLOTUREE || statut == StatutReclamation.RESOLUE;
    }

    @Transactional(readOnly = true)
    public CsatEvaluationStatusDTO getStatutPourReclamation(Long reclamationId) {
        return csatEvaluationRepository
                .findByReclamationId(reclamationId)
                .map(e -> CsatEvaluationStatusDTO.builder().existe(true).evaluation(versDto(e)).build())
                .orElseGet(() -> CsatEvaluationStatusDTO.builder().existe(false).evaluation(null).build());
    }

    @Transactional(readOnly = true)
    public CsatStatistiquesDTO getMoyenneGlobale() {
        Double moyenne = csatEvaluationRepository.findAverageNote();
        double valeur = moyenne != null ? Math.round(moyenne * 100.0) / 100.0 : 0.0;
        return CsatStatistiquesDTO.builder().moyenneNote(valeur).build();
    }

    private static CsatEvaluationResponseDTO versDto(CsatEvaluation e) {
        return CsatEvaluationResponseDTO.builder()
                .id(e.getId())
                .reclamationId(e.getReclamationId())
                .userId(e.getUserId())
                .note(e.getNote())
                .commentaire(e.getCommentaire())
                .dateEvaluation(e.getDateEvaluation())
                .build();
    }
}
