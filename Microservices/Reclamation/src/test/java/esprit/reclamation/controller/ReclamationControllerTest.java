package esprit.reclamation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import esprit.reclamation.dto.ReclamationResponseDTO;
import esprit.reclamation.dto.ReponseAdminDTO;
import esprit.reclamation.entities.Priorite;
import esprit.reclamation.entities.StatutReclamation;
import esprit.reclamation.services.FraudClientService;
import esprit.reclamation.services.ReclamationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReclamationController.class)
class ReclamationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReclamationService reclamationService;

    @MockBean
    private FraudClientService fraudClientService;

    @Test
    void post_creer_retourne201_et_appelleFraud() throws Exception {
        ReclamationResponseDTO body =
                ReclamationResponseDTO.builder()
                        .id(1L)
                        .titre("T")
                        .description("D")
                        .statut(StatutReclamation.EN_ATTENTE)
                        .priorite(Priorite.MOYENNE)
                        .userId(1L)
                        .dateCreation(LocalDateTime.now())
                        .build();
        when(reclamationService.creer(any())).thenReturn(body);

        mockMvc.perform(
                        post("/api/reclamations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"titre\":\"T\",\"description\":\"D\",\"userId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"));

        verify(fraudClientService)
                .analyzeAction(eq(1L), eq("RECLAMATION_CREATE"), any(), any());
    }

    @Test
    void get_all_retourne200() throws Exception {
        when(reclamationService.getAll()).thenReturn(java.util.List.of());
        mockMvc.perform(get("/api/reclamations")).andExpect(status().isOk());
    }

    @Test
    void put_repondre_retourne200() throws Exception {
        ReclamationResponseDTO out =
                ReclamationResponseDTO.builder()
                        .id(4L)
                        .titre("T")
                        .description("D")
                        .statut(StatutReclamation.EN_COURS)
                        .priorite(Priorite.MOYENNE)
                        .userId(1L)
                        .reponse("OK")
                        .dateCreation(LocalDateTime.now())
                        .build();
        when(reclamationService.repondre(eq(4L), any(ReponseAdminDTO.class))).thenReturn(out);

        ReponseAdminDTO dto = new ReponseAdminDTO();
        dto.setAdminId(2L);
        dto.setReponse("OK");
        dto.setStatut(StatutReclamation.EN_COURS);

        mockMvc.perform(
                        put("/api/reclamations/4/repondre")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reponse").value("OK"));
    }

    @Test
    void get_parUser_retourne200() throws Exception {
        when(reclamationService.getByUserId(14L)).thenReturn(java.util.List.of());
        mockMvc.perform(get("/api/reclamations/user/14")).andExpect(status().isOk());
    }
}
