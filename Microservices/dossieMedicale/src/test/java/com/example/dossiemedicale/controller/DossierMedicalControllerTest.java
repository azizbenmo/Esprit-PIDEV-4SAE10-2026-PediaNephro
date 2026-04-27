package com.example.dossiemedicale.controller;

import com.example.dossiemedicale.DTO.AnalyseEvolutionResponse;
import com.example.dossiemedicale.DTO.ComparaisonHospitalisationResponse;
import com.example.dossiemedicale.DTO.DossierMedicalRequest;
import com.example.dossiemedicale.DTO.DossierResumeResponse;
import com.example.dossiemedicale.DTO.RapportConsultationParentResponse;
import com.example.dossiemedicale.entities.DossierMedical;
import com.example.dossiemedicale.services.ComparaisonHospitalisationService;
import com.example.dossiemedicale.services.DossierEvolutionCliniqueService;
import com.example.dossiemedicale.services.DossierMedicalService;
import com.example.dossiemedicale.services.DossierResumeService;
import com.example.dossiemedicale.services.RapportConsultationExterneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DossierMedicalControllerTest {

    @Mock
    private DossierMedicalService dossierMedicalService;

    @Mock
    private DossierResumeService dossierResumeService;

    @Mock
    private DossierEvolutionCliniqueService dossierEvolutionCliniqueService;

    @Mock
    private RapportConsultationExterneService rapportConsultationExterneService;

    @Mock
    private ComparaisonHospitalisationService comparaisonService;

    private DossierMedicalController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new DossierMedicalController(
                dossierMedicalService,
                dossierResumeService,
                dossierEvolutionCliniqueService,
                rapportConsultationExterneService,
                comparaisonService
        );
    }

    @Test
    void ajouterDossierMedical_shouldReturnCreatedStatus() {
        DossierMedicalRequest request = new DossierMedicalRequest();
        request.setEnfantId(6L);
        request.setDateCreation(LocalDate.of(2026, 4, 15));

        DossierMedical dossier = new DossierMedical();
        dossier.setIdDossier(1L);

        when(dossierMedicalService.ajouterDossierMedical(request)).thenReturn(dossier);

        ResponseEntity<DossierMedical> response = controller.ajouterDossierMedical(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dossier, response.getBody());
    }

    @Test
    void getDossiersByPatient_shouldReturnRepositoryResult() {
        List<DossierMedical> dossiers = List.of(new DossierMedical(), new DossierMedical());

        when(dossierMedicalService.getDossiersByPatientId(3L)).thenReturn(dossiers);

        ResponseEntity<List<DossierMedical>> response = controller.getDossiersByPatient(3L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dossiers, response.getBody());
    }

    @Test
    void supprimerDossierMedical_shouldReturnNoContent() {
        ResponseEntity<Void> response = controller.supprimerDossierMedical(11L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(dossierMedicalService).supprimerDossierMedical(11L);
    }

    @Test
    void getResumeAvance_shouldReturnServiceResult() {
        DossierResumeResponse resume = new DossierResumeResponse();

        when(dossierResumeService.getResumeComplet(14L)).thenReturn(resume);

        ResponseEntity<DossierResumeResponse> response = controller.getResumeAvance(14L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(resume, response.getBody());
    }

    @Test
    void analyserEvolution_shouldReturnServiceResult() {
        AnalyseEvolutionResponse analyse = new AnalyseEvolutionResponse();

        when(dossierEvolutionCliniqueService.analyserEvolution(9L)).thenReturn(analyse);

        ResponseEntity<AnalyseEvolutionResponse> response = controller.analyserEvolution(9L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(analyse, response.getBody());
    }

    @Test
    void comparer_shouldReturnServiceResult() {
        ComparaisonHospitalisationResponse comparaison = new ComparaisonHospitalisationResponse();

        when(comparaisonService.comparer(8L)).thenReturn(comparaison);

        ResponseEntity<ComparaisonHospitalisationResponse> response = controller.comparer(8L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(comparaison, response.getBody());
    }

    @Test
    void getRapportsParent_shouldPassAuthorizationHeaderToService() {
        List<RapportConsultationParentResponse> rapports = List.of();
        String authorizationHeader = "Bearer token-123";

        when(rapportConsultationExterneService.getRapportsPourParentConnecte(5L, authorizationHeader))
                .thenReturn(rapports);

        ResponseEntity<List<RapportConsultationParentResponse>> response =
                controller.getRapportsParent(5L, authorizationHeader);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(rapports, response.getBody());
    }
}
