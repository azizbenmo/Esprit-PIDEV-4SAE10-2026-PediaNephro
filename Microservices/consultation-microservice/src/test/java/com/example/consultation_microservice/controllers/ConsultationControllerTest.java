package com.example.consultation_microservice.controllers;

import com.example.consultation_microservice.entities.Consultation;
import com.example.consultation_microservice.entities.ConsultationStatus;
import com.example.consultation_microservice.entities.Medecin;
import com.example.consultation_microservice.entities.NiveauUrgence;
import com.example.consultation_microservice.entities.Patient;
import com.example.consultation_microservice.repositories.ConsultationRepository;
import com.example.consultation_microservice.repositories.MedecinRepository;
import com.example.consultation_microservice.repositories.PatientRepository;
import com.example.consultation_microservice.services.EmailService;
import com.example.consultation_microservice.services.TriageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultationControllerTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private MedecinRepository medecinRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private TriageService triageService;

    @InjectMocks
    private ConsultationController consultationController;

    @Test
    void createDemandeConsultation_shouldReturnConflictWhenSlotIsAlreadyTaken() {
        LocalDateTime desiredDate = LocalDateTime.of(2026, 4, 20, 10, 0);
        Consultation consultation = buildConsultationRequest(desiredDate);
        Patient existingPatient = buildPatient(99L, 99L, "Kid", "One", "kid@example.com");
        Medecin medecin = buildMedecin(10L, 7L, "House", "Gregory", "doctor@example.com");

        when(patientRepository.findByUserId(99L)).thenReturn(Optional.of(existingPatient));
        when(medecinRepository.findByUserId(7L)).thenReturn(Optional.of(medecin));
        when(consultationRepository.existsCreneauPris(10L, desiredDate)).thenReturn(true);

        ResponseEntity<?> response = consultationController.createDemandeConsultation(consultation);

        assertEquals(409, response.getStatusCode().value());
        Map<?, ?> body = assertInstanceOf(Map.class, response.getBody());
        assertEquals("CRENEAU_CONFLIT", body.get("code"));
        verify(consultationRepository, never()).save(any(Consultation.class));
        verify(triageService, never()).calculerTriage(any(Consultation.class));
        verify(emailService, never()).sendConfirmation(any(), any());
    }

    @Test
    void createDemandeConsultation_shouldCreatePatientSaveConsultationAndSendEmails() {
        LocalDateTime desiredDate = LocalDateTime.of(2026, 4, 21, 11, 0);
        Consultation consultation = buildConsultationRequest(desiredDate);
        consultation.setDemandeurParentEmail("parent@example.com");

        Medecin medecin = buildMedecin(10L, 7L, "House", "Gregory", "doctor@example.com");

        when(patientRepository.findByUserId(99L)).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient patient = invocation.getArgument(0);
            patient.setId(501L);
            return patient;
        });
        when(medecinRepository.findByUserId(7L)).thenReturn(Optional.of(medecin));
        when(consultationRepository.existsCreneauPris(10L, desiredDate)).thenReturn(false);
        when(triageService.calculerTriage(any(Consultation.class))).thenAnswer(invocation -> {
            Consultation value = invocation.getArgument(0);
            value.setNiveauUrgence(NiveauUrgence.URGENTE);
            value.setScoreUrgence(80);
            value.setJustificationTriage("critical");
            return value;
        });
        when(consultationRepository.save(any(Consultation.class))).thenAnswer(invocation -> {
            Consultation saved = invocation.getArgument(0);
            saved.setId(900L);
            return saved;
        });

        ResponseEntity<?> response = consultationController.createDemandeConsultation(consultation);

        assertEquals(200, response.getStatusCode().value());
        Consultation saved = assertInstanceOf(Consultation.class, response.getBody());
        assertEquals(ConsultationStatus.DEMANDEE, saved.getStatut());
        assertEquals(NiveauUrgence.URGENTE, saved.getNiveauUrgence());
        assertEquals(900L, saved.getId());
        verify(patientRepository).save(any(Patient.class));
        verify(consultationRepository).save(any(Consultation.class));
        verify(emailService).sendConfirmation("parent@example.com", saved);
        verify(emailService).sendConfirmation("kid@example.com", saved);
        verify(emailService).sendAlerteUrgente("doctor@example.com", saved);
    }

    @Test
    void annulerConsultation_shouldUpdateStatusAndSendCancellationEmail() {
        Consultation consultation = new Consultation();
        consultation.setId(12L);
        consultation.setStatut(ConsultationStatus.ACCEPTEE);
        consultation.setPatient(buildPatient(3L, 3L, "Kid", "One", "kid@example.com"));
        consultation.setMedecin(buildMedecin(10L, 7L, "House", "Gregory", "doctor@example.com"));

        when(consultationRepository.findById(12L)).thenReturn(Optional.of(consultation));
        when(consultationRepository.save(any(Consultation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Consultation result = consultationController.annulerConsultation(12L);

        assertEquals(ConsultationStatus.ANNULEE, result.getStatut());
        verify(emailService).sendAnnulation("kid@example.com", result);
    }

    @Test
    void annulerConsultation_shouldRejectFinishedConsultation() {
        Consultation consultation = new Consultation();
        consultation.setId(15L);
        consultation.setStatut(ConsultationStatus.TERMINEE);

        when(consultationRepository.findById(15L)).thenReturn(Optional.of(consultation));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> consultationController.annulerConsultation(15L));

        assertTrue(ex.getMessage().contains("Impossible"));
        verify(consultationRepository, never()).save(any(Consultation.class));
        verify(emailService, never()).sendAnnulation(any(), any());
    }

    private Consultation buildConsultationRequest(LocalDateTime desiredDate) {
        Consultation consultation = new Consultation();
        consultation.setDateSouhaitee(desiredDate);
        consultation.setSpecialite("Nephrologie Pediatrique");
        consultation.setMotif("controle");

        Patient patient = new Patient();
        patient.setId(99L);
        patient.setUserId(99L);
        patient.setNom("One");
        patient.setPrenom("Kid");
        patient.setEmail("kid@example.com");
        consultation.setPatient(patient);

        Medecin medecin = new Medecin();
        medecin.setUserId(7L);
        consultation.setMedecin(medecin);

        return consultation;
    }

    private Patient buildPatient(Long id, Long userId, String prenom, String nom, String email) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setUserId(userId);
        patient.setPrenom(prenom);
        patient.setNom(nom);
        patient.setEmail(email);
        return patient;
    }

    private Medecin buildMedecin(Long id, Long userId, String nom, String prenom, String email) {
        Medecin medecin = new Medecin();
        medecin.setId(id);
        medecin.setUserId(userId);
        medecin.setNom(nom);
        medecin.setPrenom(prenom);
        medecin.setEmail(email);
        medecin.setDisponible(true);
        return medecin;
    }
}
