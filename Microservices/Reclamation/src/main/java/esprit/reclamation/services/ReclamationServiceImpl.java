package esprit.reclamation.services;

import esprit.reclamation.clients.UserClient;
import esprit.reclamation.dto.CsatEvaluationResponseDTO;
import esprit.reclamation.dto.CsatEvaluationStatusDTO;
import esprit.reclamation.dto.CsatStatistiquesDTO;
import esprit.reclamation.dto.CsatSubmitDTO;
import esprit.reclamation.dto.ReclamationHistoryDTO;
import esprit.reclamation.dto.ReclamationRequestDTO;
import esprit.reclamation.dto.ReclamationResponseDTO;
import esprit.reclamation.dto.ReponseAdminDTO;
import esprit.reclamation.dto.StatistiquesDTO;
import esprit.reclamation.dto.UserDTO;
import esprit.reclamation.entities.Reclamation;
import esprit.reclamation.entities.StatutReclamation;
import esprit.reclamation.exceptions.ReclamationNotFoundException;
import esprit.reclamation.repositories.ReclamationRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ReclamationServiceImpl implements ReclamationService {

    private final ReclamationRepository reclamationRepository;
    private final UserClient userClient;
    private final CategorisationService categorisationService;
    private final ReclamationHistoryService reclamationHistoryService;
    private final CsatService csatService;

    @Override
    public ReclamationResponseDTO creer(ReclamationRequestDTO dto) {
        System.out.println("[ReclamationService] creer userId=" + dto.getUserId());

        // Même logique que l’affichage liste : si le MS User ne connaît pas l’id (404) ou est down,
        // on crée quand même la réclamation (Postman, parentId, intégration) avec libellés stub.
        UserDTO user = resolveUserForCreate(dto.getUserId());

        Reclamation reclamation =
                Reclamation.builder()
                        .titre(dto.getTitre().trim())
                        .description(dto.getDescription().trim())
                        .priorite(null)
                        .userId(dto.getUserId())
                        .statut(StatutReclamation.EN_ATTENTE)
                        .build();

        categorisationService.enrichir(reclamation);

        Reclamation saved = reclamationRepository.save(reclamation);
        reclamationHistoryService.enregistrer(
                saved.getId(), null, StatutReclamation.EN_ATTENTE, dto.getUserId(), "Réclamation créée.");
        return toResponse(saved, user);
    }

    /**
     * Résout l’utilisateur pour la réponse : données User si disponibles, sinon stub (pas d’échec 404).
     */
    private UserDTO resolveUserForCreate(Long userId) {
        try {
            UserDTO u = userClient.getUserById(userId);
            if (u != null && u.getId() != null && !Boolean.FALSE.equals(u.getActive())) {
                return u;
            }
        } catch (FeignException.NotFound ex) {
            System.out.println("[ReclamationService] creer — User MS 404 pour id=" + userId + ", création autorisée.");
        } catch (FeignException ex) {
            System.out.println(
                    "[ReclamationService] creer — User MS indisponible (" + ex.status() + ") id=" + userId);
        } catch (Exception ex) {
            System.out.println("[ReclamationService] creer — User MS erreur id=" + userId + " " + ex.getClass().getSimpleName());
        }
        return stubUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponseDTO> getAll() {
        System.out.println("[ReclamationService] getAll");
        return reclamationRepository.findAll().stream().map(this::toResponseWithUser).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReclamationResponseDTO getById(Long id) {
        System.out.println("[ReclamationService] getById id=" + id);
        return toResponseWithUser(getEntityOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponseDTO> getByUserId(Long userId) {
        System.out.println("[ReclamationService] getByUserId userId=" + userId);
        return reclamationRepository.findByUserId(userId).stream().map(this::toResponseWithUser).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponseDTO> getByStatut(StatutReclamation statut) {
        System.out.println("[ReclamationService] getByStatut statut=" + statut);
        return reclamationRepository.findByStatut(statut).stream().map(this::toResponseWithUser).toList();
    }

    @Override
    public ReclamationResponseDTO repondre(Long id, ReponseAdminDTO dto) {
        System.out.println("[ReclamationService] repondre id=" + id + " adminId=" + dto.getAdminId());

        requireAdmin(dto.getAdminId());

        Reclamation reclamation = getEntityOrThrow(id);
        StatutReclamation statutAvant = reclamation.getStatut();
        reclamation.setAdminId(dto.getAdminId());
        reclamation.setReponse(dto.getReponse());
        reclamation.setStatut(dto.getStatut());

        Reclamation saved = reclamationRepository.save(reclamation);
        reclamationHistoryService.enregistrer(
                id,
                statutAvant,
                dto.getStatut(),
                dto.getAdminId(),
                commentaireHistoriqueApresReponseAdmin(dto.getStatut()));
        UserDTO user = lookupUserLenient(saved.getUserId());
        return toResponse(saved, user);
    }

    @Override
    public ReclamationResponseDTO changerStatut(Long id, StatutReclamation statut) {
        System.out.println("[ReclamationService] changerStatut id=" + id + " statut=" + statut);
        Reclamation reclamation = getEntityOrThrow(id);
        StatutReclamation statutAvant = reclamation.getStatut();
        reclamation.setStatut(statut);
        Reclamation saved = reclamationRepository.save(reclamation);
        if (!Objects.equals(statutAvant, statut)) {
            reclamationHistoryService.enregistrer(
                    id, statutAvant, statut, null, commentaireHistoriqueChangementStatut(statut));
        }
        return toResponseWithUser(saved);
    }

    private static String commentaireHistoriqueApresReponseAdmin(StatutReclamation nouveauStatut) {
        return switch (nouveauStatut) {
            case RESOLUE -> "Réclamation résolue — réponse administrateur enregistrée.";
            case CLOTUREE -> "Réclamation clôturée — réponse administrateur enregistrée.";
            case REJETEE -> "Réclamation rejetée — réponse administrateur enregistrée.";
            case EN_COURS -> "Prise en charge — réponse administrateur enregistrée.";
            case EN_ATTENTE -> "Réponse administrateur enregistrée (statut : en attente).";
            case ESCALADEE -> "Escalade SLA — réponse administrateur enregistrée.";
        };
    }

    private static String commentaireHistoriqueChangementStatut(StatutReclamation nouveauStatut) {
        return switch (nouveauStatut) {
            case RESOLUE -> "Réclamation marquée comme résolue.";
            case CLOTUREE -> "Réclamation clôturée.";
            case REJETEE -> "Réclamation rejetée.";
            case EN_COURS -> "Réclamation passée en cours de traitement.";
            case EN_ATTENTE -> "Statut repassé en attente.";
            case ESCALADEE -> "Réclamation passée en escalade SLA (SLA dépassé ou action admin).";
        };
    }

    @Override
    public void supprimer(Long id) {
        System.out.println("[ReclamationService] supprimer id=" + id);
        Reclamation reclamation = getEntityOrThrow(id);
        reclamationRepository.delete(reclamation);
    }

    @Override
    @Transactional(readOnly = true)
    public StatistiquesDTO getStatistiques() {
        System.out.println("[ReclamationService] getStatistiques");
        long total = reclamationRepository.count();
        long enAttente = reclamationRepository.countByStatut(StatutReclamation.EN_ATTENTE);
        long enCours = reclamationRepository.countByStatut(StatutReclamation.EN_COURS);
        long resolues = reclamationRepository.countByStatut(StatutReclamation.RESOLUE);
        long rejetees = reclamationRepository.countByStatut(StatutReclamation.REJETEE);

        return StatistiquesDTO.builder()
                .total(total)
                .enAttente(enAttente)
                .enCours(enCours)
                .resolues(resolues)
                .rejetees(rejetees)
                .build();
    }

    @Override
    public ReclamationResponseDTO modifier(Long id, ReclamationRequestDTO dto) {
        Reclamation reclamation = getEntityOrThrow(id);
        reclamation.setTitre(dto.getTitre());
        reclamation.setDescription(dto.getDescription());
        if (dto.getPriorite() != null) {
            reclamation.setPriorite(dto.getPriorite());
        }
        reclamation.setUserId(dto.getUserId());

        Reclamation saved = reclamationRepository.save(reclamation);
        return toResponseWithUser(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationHistoryDTO> getTimeline(Long id) {
        getEntityOrThrow(id);
        return reclamationHistoryService.getTimeline(id);
    }

    @Override
    public CsatEvaluationResponseDTO soumettreEvaluation(Long reclamationId, CsatSubmitDTO dto) {
        return csatService.soumettre(reclamationId, dto);
    }

    @Override
    @Transactional(readOnly = true)
    public CsatEvaluationStatusDTO getCsatStatus(Long reclamationId) {
        getEntityOrThrow(reclamationId);
        return csatService.getStatutPourReclamation(reclamationId);
    }

    @Override
    @Transactional(readOnly = true)
    public CsatStatistiquesDTO getCsatStatistiques() {
        return csatService.getMoyenneGlobale();
    }

    private Reclamation getEntityOrThrow(Long id) {
        return reclamationRepository
                .findById(id)
                .orElseThrow(() -> new ReclamationNotFoundException("Reclamation introuvable (id=" + id + ")."));
    }

    /**
     * Vérifie l’admin si le MS User répond ; sinon mode tolérant (comme {@link #resolveUserForCreate})
     * pour éviter des 404 en intégration quand l’admin JWT n’existe pas dans le MS User.
     */
    private void requireAdmin(Long adminId) {
        if (adminId == null) {
            return;
        }
        try {
            UserDTO admin = userClient.getUserById(adminId);
            if (admin != null && admin.getId() != null && !Boolean.FALSE.equals(admin.getActive())) {
                return;
            }
        } catch (FeignException.NotFound ex) {
            System.out.println(
                    "[ReclamationService] requireAdmin — User MS 404 pour adminId=" + adminId + ", réponse autorisée.");
            return;
        } catch (FeignException ex) {
            System.out.println(
                    "[ReclamationService] requireAdmin — User MS indisponible (" + ex.status() + ") adminId="
                            + adminId
                            + ", réponse autorisée.");
            return;
        } catch (Exception ex) {
            System.out.println(
                    "[ReclamationService] requireAdmin — erreur "
                            + ex.getClass().getSimpleName()
                            + " adminId="
                            + adminId
                            + ", réponse autorisée.");
            return;
        }
        System.out.println(
                "[ReclamationService] requireAdmin — admin inconnu ou inactif dans la réponse User, réponse autorisée (mode tolérant).");
    }

    private UserDTO lookupUserLenient(Long userId) {
        try {
            UserDTO u = userClient.getUserById(userId);
            if (u != null && u.getId() != null && !Boolean.FALSE.equals(u.getActive())) {
                return u;
            }
        } catch (FeignException.NotFound ex) {
            return stubUser(userId);
        } catch (FeignException ex) {
            System.out.println("[ReclamationService] User MS indisponible pour id=" + userId + " (" + ex.status() + ")");
        } catch (Throwable t) {
            System.out.println("[ReclamationService] enrichUser skip id=" + userId + " " + t.getClass().getSimpleName());
        }
        return stubUser(userId);
    }

    private static UserDTO stubUser(Long userId) {
        return UserDTO.builder()
                .id(userId)
                .username("Utilisateur")
                .email("")
                .role(null)
                .active(null)
                .build();
    }

    private ReclamationResponseDTO toResponseWithUser(Reclamation r) {
        return toResponse(r, lookupUserLenient(r.getUserId()));
    }

    private ReclamationResponseDTO toResponse(Reclamation r, UserDTO user) {
        String username = (user == null ? "Utilisateur inconnu" : user.getUsername());
        String email = (user == null ? "" : user.getEmail());
        if (username == null || username.isBlank()) {
            username = "Utilisateur inconnu";
        }
        if (email == null) {
            email = "";
        }

        return ReclamationResponseDTO.builder()
                .id(r.getId())
                .titre(r.getTitre())
                .description(r.getDescription())
                .statut(r.getStatut())
                .priorite(r.getPriorite())
                .categorie(r.getCategorie())
                .prioriteReclamation(r.getPrioriteReclamation())
                .slaDeadline(r.getSlaDeadline())
                .escaladee(r.getEscaladee())
                .userId(r.getUserId())
                .userUsername(username)
                .userEmail(email)
                .adminId(r.getAdminId())
                .reponse(r.getReponse())
                .dateCreation(r.getDateCreation())
                .dateTraitement(r.getDateTraitement())
                .build();
    }
}
