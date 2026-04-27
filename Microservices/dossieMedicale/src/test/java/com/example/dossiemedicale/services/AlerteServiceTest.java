package com.example.dossiemedicale.services;

import com.example.dossiemedicale.DTO.AlerteRequest;
import com.example.dossiemedicale.entities.Alerte;
import com.example.dossiemedicale.entities.ConstanteVitale;
import com.example.dossiemedicale.repositoories.AlerteRepository;
import com.example.dossiemedicale.repositoories.ConstanteVitaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlerteServiceTest {

    @Mock
    private AlerteRepository alerteRepository;

    @Mock
    private ConstanteVitaleRepository constanteVitaleRepository;

    @InjectMocks
    private AlerteService alerteService;

    @Test
    void ajouterAlerte_shouldAttachConstanteWhenConstanteIdIsProvided() {
        AlerteRequest request = new AlerteRequest();
        request.setNiveau("CRITIQUE");
        request.setMessage("Tension elevee");
        request.setDateDeclenchement(new Date());
        request.setConstanteId(9L);

        ConstanteVitale constante = new ConstanteVitale();
        constante.setIdConstante(9L);

        when(constanteVitaleRepository.findById(9L)).thenReturn(Optional.of(constante));
        when(alerteRepository.save(any(Alerte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Alerte result = alerteService.ajouterAlerte(request);

        assertEquals("CRITIQUE", result.getNiveau());
        assertEquals("Tension elevee", result.getMessage());
        assertSame(constante, result.getConstante());
    }

    @Test
    void ajouterAlerte_shouldThrowWhenConstanteDoesNotExist() {
        AlerteRequest request = new AlerteRequest();
        request.setConstanteId(100L);

        when(constanteVitaleRepository.findById(100L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> alerteService.ajouterAlerte(request)
        );

        assertTrue(exception.getMessage().contains("Constante introuvable"));
    }

    @Test
    void modifierAlerte_shouldUpdateExistingAlert() {
        Date now = new Date();
        AlerteRequest request = new AlerteRequest();
        request.setNiveau("MODEREE");
        request.setMessage("Controle necessaire");
        request.setDateDeclenchement(now);
        request.setConstanteId(5L);

        Alerte existing = new Alerte();
        existing.setIdAlerte(2L);

        ConstanteVitale constante = new ConstanteVitale();
        constante.setIdConstante(5L);

        when(alerteRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(constanteVitaleRepository.findById(5L)).thenReturn(Optional.of(constante));
        when(alerteRepository.save(any(Alerte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Alerte result = alerteService.modifierAlerte(2L, request);

        assertEquals("MODEREE", result.getNiveau());
        assertEquals("Controle necessaire", result.getMessage());
        assertEquals(now, result.getDateDeclenchement());
        assertSame(constante, result.getConstante());
    }

    @Test
    void getAlertesByDossier_shouldReturnRepositoryResult() {
        List<Alerte> expected = List.of(new Alerte(), new Alerte());

        when(alerteRepository.findByConstante_Dossier_IdDossierOrderByDateDeclenchementDesc(4L)).thenReturn(expected);

        List<Alerte> result = alerteService.getAlertesByDossier(4L);

        assertSame(expected, result);
    }

    @Test
    void supprimerAlerte_shouldDelegateToRepository() {
        alerteService.supprimerAlerte(8L);

        verify(alerteRepository).deleteById(8L);
    }
}
