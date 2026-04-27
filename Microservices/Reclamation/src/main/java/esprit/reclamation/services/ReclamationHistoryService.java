package esprit.reclamation.services;

import esprit.reclamation.dto.ReclamationHistoryDTO;
import esprit.reclamation.entities.ReclamationHistory;
import esprit.reclamation.entities.StatutReclamation;
import esprit.reclamation.repositories.ReclamationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReclamationHistoryService {

    private final ReclamationHistoryRepository reclamationHistoryRepository;

    @Transactional
    public void enregistrer(
            Long reclamationId,
            StatutReclamation ancienStatut,
            StatutReclamation nouveauStatut,
            Long acteurId,
            String commentaire) {
        if (Objects.equals(ancienStatut, nouveauStatut) && !StringUtils.hasText(commentaire)) {
            return;
        }
        ReclamationHistory ligne =
                ReclamationHistory.builder()
                        .reclamationId(reclamationId)
                        .ancienStatut(ancienStatut)
                        .nouveauStatut(nouveauStatut)
                        .acteurId(acteurId)
                        .commentaire(commentaire)
                        .build();
        reclamationHistoryRepository.save(ligne);
        log.debug("Historique réclamation {} : {} -> {}", reclamationId, ancienStatut, nouveauStatut);
    }

    @Transactional(readOnly = true)
    public List<ReclamationHistoryDTO> getTimeline(Long reclamationId) {
        return reclamationHistoryRepository.findByReclamationIdOrderByDateChangementAsc(reclamationId).stream()
                .map(this::versDto)
                .toList();
    }

    private ReclamationHistoryDTO versDto(ReclamationHistory h) {
        return ReclamationHistoryDTO.builder()
                .id(h.getId())
                .reclamationId(h.getReclamationId())
                .ancienStatut(h.getAncienStatut())
                .nouveauStatut(h.getNouveauStatut())
                .acteurId(h.getActeurId())
                .commentaire(h.getCommentaire())
                .dateChangement(h.getDateChangement())
                .build();
    }
}
