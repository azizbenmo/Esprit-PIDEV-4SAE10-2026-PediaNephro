package esprit.reclamation.services;

import esprit.reclamation.dto.CsatSubmitDTO;
import esprit.reclamation.entities.CsatEvaluation;
import esprit.reclamation.entities.Priorite;
import esprit.reclamation.entities.Reclamation;
import esprit.reclamation.entities.StatutReclamation;
import esprit.reclamation.exceptions.DuplicateCsatException;
import esprit.reclamation.exceptions.IllegalReclamationStateException;
import esprit.reclamation.repositories.CsatEvaluationRepository;
import esprit.reclamation.repositories.ReclamationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsatServiceTest {

    @Mock
    private CsatEvaluationRepository csatEvaluationRepository;

    @Mock
    private ReclamationRepository reclamationRepository;

    private CsatService service;

    @BeforeEach
    void setUp() {
        service = new CsatService(csatEvaluationRepository, reclamationRepository);
    }

    @Test
    void soumettre_reussit_quandReclamationResolue_etPasDejaEvaluee() {
        Reclamation rec =
                Reclamation.builder()
                        .id(10L)
                        .titre("T")
                        .description("D")
                        .statut(StatutReclamation.RESOLUE)
                        .priorite(Priorite.MOYENNE)
                        .userId(1L)
                        .escaladee(false)
                        .build();
        when(reclamationRepository.findById(10L)).thenReturn(Optional.of(rec));
        when(csatEvaluationRepository.findByReclamationId(10L)).thenReturn(Optional.empty());
        when(csatEvaluationRepository.save(any(CsatEvaluation.class)))
                .thenAnswer(
                        inv -> {
                            CsatEvaluation e = inv.getArgument(0);
                            e.setId(77L);
                            return e;
                        });

        CsatSubmitDTO dto = new CsatSubmitDTO();
        dto.setUserId(1L);
        dto.setNote(5);
        dto.setCommentaire("Très bien");

        var out = service.soumettre(10L, dto);

        assertThat(out.getNote()).isEqualTo(5);
        assertThat(out.getReclamationId()).isEqualTo(10L);
        verify(csatEvaluationRepository).save(any());
    }

    @Test
    void soumettre_refuse_siStatutNonEligible() {
        Reclamation rec =
                Reclamation.builder()
                        .id(11L)
                        .titre("T")
                        .description("D")
                        .statut(StatutReclamation.EN_ATTENTE)
                        .priorite(Priorite.MOYENNE)
                        .userId(1L)
                        .escaladee(false)
                        .build();
        when(reclamationRepository.findById(11L)).thenReturn(Optional.of(rec));

        CsatSubmitDTO dto = new CsatSubmitDTO();
        dto.setUserId(1L);
        dto.setNote(4);
        dto.setCommentaire("x");

        assertThatThrownBy(() -> service.soumettre(11L, dto))
                .isInstanceOf(IllegalReclamationStateException.class)
                .hasMessageContaining("Évaluation impossible");
    }

    @Test
    void soumettre_refuse_siDejaEvalue() {
        Reclamation rec =
                Reclamation.builder()
                        .id(12L)
                        .titre("T")
                        .description("D")
                        .statut(StatutReclamation.CLOTUREE)
                        .priorite(Priorite.MOYENNE)
                        .userId(1L)
                        .escaladee(false)
                        .build();
        when(reclamationRepository.findById(12L)).thenReturn(Optional.of(rec));
        when(csatEvaluationRepository.findByReclamationId(12L)).thenReturn(Optional.of(new CsatEvaluation()));

        CsatSubmitDTO dto = new CsatSubmitDTO();
        dto.setUserId(1L);
        dto.setNote(3);
        dto.setCommentaire("y");

        assertThatThrownBy(() -> service.soumettre(12L, dto)).isInstanceOf(DuplicateCsatException.class);
    }

    @Test
    void getMoyenneGlobale_zeroSiAucuneNote() {
        when(csatEvaluationRepository.findAverageNote()).thenReturn(null);
        assertThat(service.getMoyenneGlobale().getMoyenneNote()).isEqualTo(0.0);
    }

    @Test
    void getMoyenneGlobale_arronditADeuxDecimales() {
        when(csatEvaluationRepository.findAverageNote()).thenReturn(4.567);
        assertThat(service.getMoyenneGlobale().getMoyenneNote()).isEqualTo(4.57);
    }
}
