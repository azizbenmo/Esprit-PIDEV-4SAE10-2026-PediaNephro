package com.example.consultation_microservice.controllers;

import com.example.consultation_microservice.entities.Avis;
import com.example.consultation_microservice.entities.Medecin;
import com.example.consultation_microservice.entities.Patient;
import com.example.consultation_microservice.repositories.AvisRepository;
import com.example.consultation_microservice.repositories.ConsultationRepository;
import com.example.consultation_microservice.repositories.MedecinRepository;
import com.example.consultation_microservice.repositories.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvisControllerTest {

    @Mock
    private AvisRepository avisRepository;

    @Mock
    private MedecinRepository medecinRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ConsultationRepository consultationRepository;

    @InjectMocks
    private AvisController avisController;

    @Test
    void soumettreAvis_shouldRejectOutOfRangeNote() {
        Map<String, Object> body = new HashMap<>();
        body.put("medecinId", 10L);
        body.put("patientId", 20L);
        body.put("note", 6);

        ResponseEntity<Map<String, Object>> response = avisController.soumettreAvis(body);

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().get("error").toString().toLowerCase().contains("note"));
        verify(avisRepository, never()).save(any(Avis.class));
    }

    @Test
    void soumettreAvis_shouldRejectDuplicateConsultationReview() {
        Map<String, Object> body = new HashMap<>();
        body.put("medecinId", 10L);
        body.put("patientId", 20L);
        body.put("note", 5);
        body.put("consultationId", 99L);

        when(avisRepository.findByConsultationId(99L)).thenReturn(Optional.of(new Avis()));

        ResponseEntity<Map<String, Object>> response = avisController.soumettreAvis(body);

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().get("error").toString().toLowerCase().contains("consultation"));
        verify(avisRepository, never()).save(any(Avis.class));
    }

    @Test
    void soumettreAvis_shouldCreatePatientWhenMissingAndReturnUpdatedRating() {
        Map<String, Object> body = new HashMap<>();
        body.put("medecinId", 10L);
        body.put("patientId", 20L);
        body.put("note", 4);
        body.put("commentaire", "Tres bon suivi");
        body.put("patientPrenom", "Ali");
        body.put("patientNom", "Ben");
        body.put("patientEmail", "ali@example.com");

        Medecin medecin = new Medecin();
        medecin.setId(10L);
        medecin.setNom("House");
        medecin.setPrenom("Gregory");

        when(medecinRepository.findById(10L)).thenReturn(Optional.of(medecin));
        when(patientRepository.findByUserId(20L)).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient patient = invocation.getArgument(0);
            patient.setId(200L);
            return patient;
        });
        when(avisRepository.findAverageNoteByMedecinId(10L)).thenReturn(4.25);
        when(avisRepository.countByMedecinId(10L)).thenReturn(3L);

        ResponseEntity<Map<String, Object>> response = avisController.soumettreAvis(body);

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> result = response.getBody();
        assertEquals(10L, result.get("medecinId"));
        assertEquals(4.3, ((Number) result.get("moyenneNote")).doubleValue());
        assertEquals(3L, result.get("nombreAvis"));

        ArgumentCaptor<Avis> avisCaptor = ArgumentCaptor.forClass(Avis.class);
        verify(avisRepository).save(avisCaptor.capture());
        Avis savedAvis = avisCaptor.getValue();
        assertEquals(4, savedAvis.getNote());
        assertEquals("Tres bon suivi", savedAvis.getCommentaire());
        assertEquals(20L, savedAvis.getPatient().getUserId());
    }

    @Test
    void soumettreAvis_shouldRejectUnknownDoctor() {
        Map<String, Object> body = new HashMap<>();
        body.put("medecinId", 404L);
        body.put("patientId", 20L);
        body.put("note", 4);

        when(medecinRepository.findById(404L)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = avisController.soumettreAvis(body);

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().get("error").toString().toLowerCase().contains("trouv"));
        verify(patientRepository, never()).findByUserId(anyLong());
        verify(avisRepository, never()).save(any(Avis.class));
    }
}
