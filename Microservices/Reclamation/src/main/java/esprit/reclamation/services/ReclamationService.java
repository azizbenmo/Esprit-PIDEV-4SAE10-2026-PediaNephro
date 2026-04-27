package esprit.reclamation.services;

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

import java.util.List;

public interface ReclamationService {

    ReclamationResponseDTO creer(ReclamationRequestDTO dto);

    List<ReclamationResponseDTO> getAll();

    ReclamationResponseDTO getById(Long id);

    List<ReclamationResponseDTO> getByUserId(Long userId);

    List<ReclamationResponseDTO> getByStatut(StatutReclamation statut);

    ReclamationResponseDTO repondre(Long id, ReponseAdminDTO dto);

    ReclamationResponseDTO changerStatut(Long id, StatutReclamation statut);

    void supprimer(Long id);

    StatistiquesDTO getStatistiques();

    ReclamationResponseDTO modifier(Long id, ReclamationRequestDTO dto);

    List<ReclamationHistoryDTO> getTimeline(Long id);

    CsatEvaluationResponseDTO soumettreEvaluation(Long reclamationId, CsatSubmitDTO dto);

    CsatEvaluationStatusDTO getCsatStatus(Long reclamationId);

    CsatStatistiquesDTO getCsatStatistiques();
}
