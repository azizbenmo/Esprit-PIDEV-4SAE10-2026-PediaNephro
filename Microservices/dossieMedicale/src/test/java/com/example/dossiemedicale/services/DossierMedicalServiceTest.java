package com.example.dossiemedicale.services;

import com.example.dossiemedicale.DTO.DossierMedicalRequest;
import com.example.dossiemedicale.entities.DossierMedical;
import com.example.dossiemedicale.entities.Enfant;
import com.example.dossiemedicale.repositoories.DossierMedicalRepository;
import com.example.dossiemedicale.repositoories.EnfantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DossierMedicalServiceTest {

    @Mock
    private DossierMedicalRepository dossierMedicalRepository;

    @Mock
    private EnfantRepository enfantRepository;

    @InjectMocks
    private DossierMedicalService dossierMedicalService;

    @Test
    void ajouterDossierMedical_shouldCreateDossierWhenChildExistsAndHasNoDossier() {
        DossierMedicalRequest request = new DossierMedicalRequest();
        request.setEnfantId(7L);
        request.setDateCreation(LocalDate.of(2026, 4, 15));

        Enfant enfant = new Enfant();
        enfant.setIdEnfant(7L);

        when(enfantRepository.findById(7L)).thenReturn(Optional.of(enfant));
        when(dossierMedicalRepository.existsByCode(any())).thenReturn(false);
        when(dossierMedicalRepository.save(any(DossierMedical.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DossierMedical result = dossierMedicalService.ajouterDossierMedical(request);

        ArgumentCaptor<DossierMedical> captor = ArgumentCaptor.forClass(DossierMedical.class);
        verify(dossierMedicalRepository).save(captor.capture());

        DossierMedical saved = captor.getValue();
        assertSame(enfant, saved.getEnfant());
        assertEquals(request.getDateCreation(), saved.getDateCreation());
        assertNotNull(saved.getCode());
        assertTrue(saved.getCode().matches("[A-Z]{3}-\\d{3}"));
        assertSame(result, saved);
    }

    @Test
    void ajouterDossierMedical_shouldThrowWhenChildDoesNotExist() {
        DossierMedicalRequest request = new DossierMedicalRequest();
        request.setEnfantId(99L);
        request.setDateCreation(LocalDate.of(2026, 4, 15));

        when(enfantRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dossierMedicalService.ajouterDossierMedical(request)
        );

        assertTrue(exception.getMessage().contains("Enfant introuvable"));
        verify(dossierMedicalRepository, never()).save(any(DossierMedical.class));
    }

    @Test
    void ajouterDossierMedical_shouldThrowWhenChildAlreadyHasDossier() {
        DossierMedicalRequest request = new DossierMedicalRequest();
        request.setEnfantId(7L);
        request.setDateCreation(LocalDate.of(2026, 4, 15));

        Enfant enfant = new Enfant();
        enfant.setIdEnfant(7L);
        enfant.setDossierMedical(new DossierMedical());

        when(enfantRepository.findById(7L)).thenReturn(Optional.of(enfant));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dossierMedicalService.ajouterDossierMedical(request)
        );

        assertTrue(exception.getMessage().contains("dossier"));
        verify(dossierMedicalRepository, never()).save(any(DossierMedical.class));
    }

    @Test
    void modifierDossierMedical_shouldUpdateExistingDossierAndKeepCurrentCode() {
        Long dossierId = 5L;
        DossierMedicalRequest request = new DossierMedicalRequest();
        request.setEnfantId(7L);
        request.setDateCreation(LocalDate.of(2026, 5, 1));

        DossierMedical existing = new DossierMedical();
        existing.setIdDossier(dossierId);
        existing.setCode("ABC-123");
        existing.setDateCreation(LocalDate.of(2026, 4, 1));

        Enfant enfant = new Enfant();
        enfant.setIdEnfant(7L);
        enfant.setDossierMedical(existing);

        when(dossierMedicalRepository.findById(dossierId)).thenReturn(Optional.of(existing));
        when(enfantRepository.findById(7L)).thenReturn(Optional.of(enfant));
        when(dossierMedicalRepository.save(any(DossierMedical.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DossierMedical result = dossierMedicalService.modifierDossierMedical(dossierId, request);

        assertEquals("ABC-123", result.getCode());
        assertEquals(request.getDateCreation(), result.getDateCreation());
        assertSame(enfant, result.getEnfant());
    }

    @Test
    void getDossiersByPatientId_shouldReturnEmptyListWhenPatientIdIsInvalid() {
        List<DossierMedical> result = dossierMedicalService.getDossiersByPatientId(0L);

        assertTrue(result.isEmpty());
        verify(dossierMedicalRepository, never()).findAllFetchedByPatientId(any());
    }

    @Test
    void getDossiersByPatientId_shouldDelegateToRepositoryWhenPatientIdIsValid() {
        DossierMedical dossier = new DossierMedical();
        List<DossierMedical> expected = List.of(dossier);

        when(dossierMedicalRepository.findAllFetchedByPatientId(3L)).thenReturn(expected);

        List<DossierMedical> result = dossierMedicalService.getDossiersByPatientId(3L);

        assertSame(expected, result);
    }

    @Test
    void getDossierMedicalById_shouldThrowWhenDossierIsMissing() {
        when(dossierMedicalRepository.findById(44L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> dossierMedicalService.getDossierMedicalById(44L)
        );

        assertInstanceOf(IllegalArgumentException.class, exception);
        assertTrue(exception.getMessage().contains("Dossier introuvable"));
    }
}
