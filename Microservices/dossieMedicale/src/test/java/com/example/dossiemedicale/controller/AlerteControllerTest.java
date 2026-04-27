package com.example.dossiemedicale.controller;

import com.example.dossiemedicale.DTO.AlerteRequest;
import com.example.dossiemedicale.entities.Alerte;
import com.example.dossiemedicale.services.AlerteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlerteControllerTest {

    @Mock
    private AlerteService alerteService;

    private AlerteController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AlerteController(alerteService);
    }

    @Test
    void ajouter_shouldReturnCreatedStatus() {
        AlerteRequest request = new AlerteRequest();
        Alerte alerte = new Alerte();
        alerte.setIdAlerte(1L);

        when(alerteService.ajouterAlerte(request)).thenReturn(alerte);

        ResponseEntity<Alerte> response = controller.ajouter(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(alerte, response.getBody());
    }

    @Test
    void diagnostic_shouldReturnCountAndAlertListWhenServiceWorks() {
        List<Alerte> alertes = List.of(new Alerte(), new Alerte());

        when(alerteService.getAllAlertes()).thenReturn(alertes);

        ResponseEntity<Map<String, Object>> response = controller.diagnostic();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().get("alertes_count"));
        assertSame(alertes, response.getBody().get("alertes"));
    }

    @Test
    void diagnostic_shouldExposeErrorMessageWhenServiceFails() {
        when(alerteService.getAllAlertes()).thenThrow(new IllegalStateException("base indisponible"));

        ResponseEntity<Map<String, Object>> response = controller.diagnostic();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("base indisponible", response.getBody().get("alertes_error"));
        assertTrue(response.getBody().containsKey("alertes_error"));
    }

    @Test
    void getByDossier_shouldReturnServiceResult() {
        List<Alerte> alertes = List.of(new Alerte());

        when(alerteService.getAlertesByDossier(4L)).thenReturn(alertes);

        List<Alerte> response = controller.getByDossier(4L);

        assertSame(alertes, response);
    }

    @Test
    void supprimer_shouldReturnNoContent() {
        ResponseEntity<Void> response = controller.supprimer(3L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(alerteService).supprimerAlerte(3L);
    }
}
