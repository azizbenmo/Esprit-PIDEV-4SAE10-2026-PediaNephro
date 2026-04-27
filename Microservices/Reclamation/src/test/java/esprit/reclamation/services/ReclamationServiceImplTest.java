package esprit.reclamation.services;

import esprit.reclamation.clients.UserClient;
import esprit.reclamation.dto.ReclamationRequestDTO;
import esprit.reclamation.dto.ReclamationResponseDTO;
import esprit.reclamation.dto.ReponseAdminDTO;
import esprit.reclamation.dto.UserDTO;
import esprit.reclamation.entities.CategorieReclamation;
import esprit.reclamation.entities.Priorite;
import esprit.reclamation.entities.Reclamation;
import esprit.reclamation.entities.StatutReclamation;
import esprit.reclamation.repositories.ReclamationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import esprit.reclamation.dto.StatistiquesDTO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReclamationServiceImplTest {

    @Mock
    private ReclamationRepository reclamationRepository;

    @Mock
    private UserClient userClient;

    private final CategorisationService categorisationService = new CategorisationService();

    @Mock
    private ReclamationHistoryService reclamationHistoryService;

    @Mock
    private CsatService csatService;

    private ReclamationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReclamationServiceImpl(
                reclamationRepository,
                userClient,
                categorisationService,
                reclamationHistoryService,
                csatService);
    }

    @Test
    void creer_enrichitCategorieEtTimeline_quandTexteFacturation() {
        when(userClient.getUserById(1L))
                .thenReturn(UserDTO.builder().id(1L).username("Jean").email("j@test.com").active(true).build());
        when(reclamationRepository.save(any(Reclamation.class)))
                .thenAnswer(
                        inv -> {
                            Reclamation r = inv.getArgument(0);
                            r.setId(50L);
                            return r;
                        });

        ReclamationRequestDTO dto = new ReclamationRequestDTO();
        dto.setTitre(" Litige ");
        dto.setDescription(" Problème de facturation et remboursement urgent ");
        dto.setUserId(1L);

        ReclamationResponseDTO out = service.creer(dto);

        assertThat(out.getCategorie()).isEqualTo(CategorieReclamation.FACTURATION);
        assertThat(out.getPrioriteReclamation()).isNotNull();
        assertThat(out.getSlaDeadline()).isNotNull();
        assertThat(out.getStatut()).isEqualTo(StatutReclamation.EN_ATTENTE);
        assertThat(out.getUserUsername()).isEqualTo("Jean");

        ArgumentCaptor<Reclamation> captor = ArgumentCaptor.forClass(Reclamation.class);
        verify(reclamationRepository).save(captor.capture());
        assertThat(captor.getValue().getCategorie()).isEqualTo(CategorieReclamation.FACTURATION);

        verify(reclamationHistoryService)
                .enregistrer(eq(50L), isNull(), eq(StatutReclamation.EN_ATTENTE), eq(1L), eq("Réclamation créée."));
    }

    @Test
    void creer_reussitAvecStub_quandUserInconnu() {
        when(userClient.getUserById(99L)).thenReturn(null);
        when(reclamationRepository.save(any(Reclamation.class)))
                .thenAnswer(
                        inv -> {
                            Reclamation r = inv.getArgument(0);
                            r.setId(7L);
                            return r;
                        });

        ReclamationRequestDTO dto = new ReclamationRequestDTO();
        dto.setTitre("Test");
        dto.setDescription("Message sans mot clé métier précis.");
        dto.setUserId(99L);

        ReclamationResponseDTO out = service.creer(dto);

        assertThat(out.getUserUsername()).isEqualTo("Utilisateur");
        assertThat(out.getUserEmail()).isEmpty();
        verify(reclamationRepository).save(any(Reclamation.class));
        verify(reclamationHistoryService).enregistrer(anyLong(), any(), any(), eq(99L), anyString());
    }

    /**
     * Comportement actuel du MS : utilisateur inactif dans User → libellés stub (pas de blocage),
     * comme pour un utilisateur inconnu.
     */
    @Test
    void creer_utiliseStub_quandUtilisateurInactif() {
        when(userClient.getUserById(2L))
                .thenReturn(UserDTO.builder().id(2L).username("Inactif").email("x@test.com").active(false).build());
        when(reclamationRepository.save(any(Reclamation.class)))
                .thenAnswer(
                        inv -> {
                            Reclamation r = inv.getArgument(0);
                            r.setId(8L);
                            return r;
                        });

        ReclamationRequestDTO dto = new ReclamationRequestDTO();
        dto.setTitre("T");
        dto.setDescription("D");
        dto.setUserId(2L);

        ReclamationResponseDTO out = service.creer(dto);

        assertThat(out.getUserUsername()).isEqualTo("Utilisateur");
        verify(reclamationRepository).save(any(Reclamation.class));
    }

    @Test
    void changerStatut_enregistreHistorique_quandStatutChange() {
        Reclamation existing =
                Reclamation.builder()
                        .id(3L)
                        .titre("T")
                        .description("D")
                        .statut(StatutReclamation.EN_ATTENTE)
                        .priorite(Priorite.MOYENNE)
                        .userId(1L)
                        .escaladee(false)
                        .build();
        when(reclamationRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(userClient.getUserById(1L))
                .thenReturn(UserDTO.builder().id(1L).username("U").email("u@test.com").active(true).build());
        when(reclamationRepository.save(any(Reclamation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReclamationResponseDTO out = service.changerStatut(3L, StatutReclamation.EN_COURS);

        assertThat(out.getStatut()).isEqualTo(StatutReclamation.EN_COURS);
        verify(reclamationHistoryService)
                .enregistrer(eq(3L), eq(StatutReclamation.EN_ATTENTE), eq(StatutReclamation.EN_COURS), isNull(), anyString());
    }

    @Test
    void changerStatut_nEnregistrePasHistorique_quandMemeStatut() {
        Reclamation existing =
                Reclamation.builder()
                        .id(3L)
                        .titre("T")
                        .description("D")
                        .statut(StatutReclamation.RESOLUE)
                        .priorite(Priorite.MOYENNE)
                        .userId(1L)
                        .escaladee(false)
                        .build();
        when(reclamationRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(userClient.getUserById(1L))
                .thenReturn(UserDTO.builder().id(1L).username("U").email("u@test.com").active(true).build());
        when(reclamationRepository.save(any(Reclamation.class))).thenAnswer(inv -> inv.getArgument(0));

        service.changerStatut(3L, StatutReclamation.RESOLUE);

        verify(reclamationHistoryService, never())
                .enregistrer(anyLong(), any(), any(), any(), anyString());
    }

    @Test
    void getStatistiques_agregeLesComptages() {
        when(reclamationRepository.count()).thenReturn(100L);
        when(reclamationRepository.countByStatut(StatutReclamation.EN_ATTENTE)).thenReturn(10L);
        when(reclamationRepository.countByStatut(StatutReclamation.EN_COURS)).thenReturn(20L);
        when(reclamationRepository.countByStatut(StatutReclamation.RESOLUE)).thenReturn(30L);
        when(reclamationRepository.countByStatut(StatutReclamation.REJETEE)).thenReturn(5L);

        StatistiquesDTO s = service.getStatistiques();

        assertThat(s.getTotal()).isEqualTo(100L);
        assertThat(s.getEnAttente()).isEqualTo(10L);
        assertThat(s.getEnCours()).isEqualTo(20L);
        assertThat(s.getResolues()).isEqualTo(30L);
        assertThat(s.getRejetees()).isEqualTo(5L);
    }

    @Test
    void repondre_reussit_quandAdminAbsentDuUserMs() {
        Reclamation existing =
                Reclamation.builder()
                        .id(4L)
                        .titre("T")
                        .description("D")
                        .statut(StatutReclamation.EN_ATTENTE)
                        .priorite(Priorite.MOYENNE)
                        .userId(1L)
                        .escaladee(false)
                        .build();

        when(reclamationRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(userClient.getUserById(10L)).thenReturn(null);
        when(userClient.getUserById(1L))
                .thenReturn(UserDTO.builder().id(1L).username("U").email("u@test.com").active(true).build());
        when(reclamationRepository.save(any(Reclamation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReponseAdminDTO dto = new ReponseAdminDTO();
        dto.setAdminId(10L);
        dto.setReponse("Réponse admin.");
        dto.setStatut(StatutReclamation.EN_COURS);

        ReclamationResponseDTO out = service.repondre(4L, dto);

        assertThat(out.getReponse()).isEqualTo("Réponse admin.");
        assertThat(out.getStatut()).isEqualTo(StatutReclamation.EN_COURS);
        verify(reclamationRepository).save(any(Reclamation.class));
        verify(reclamationHistoryService)
                .enregistrer(
                        eq(4L),
                        eq(StatutReclamation.EN_ATTENTE),
                        eq(StatutReclamation.EN_COURS),
                        eq(10L),
                        anyString());
    }
}
