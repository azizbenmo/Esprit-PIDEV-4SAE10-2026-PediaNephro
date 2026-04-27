package tn.example.events.Services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.example.events.Entities.Partenariat;
import tn.example.events.Entities.StatutPartenariat;
import tn.example.events.Repositories.PartenariatRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartenariatServiceTest {

    @Mock
    private PartenariatRepository partenariatRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private PartenariatService partenariatService;

    private Partenariat partenariat;

    @BeforeEach
    void setUp() {
        partenariat = new Partenariat();
        partenariat.setIdPartenariat(1L);
        partenariat.setNomEntreprise("Orange");
        partenariat.setEmailEntreprise("orange@test.com");
        partenariat.setStatut(StatutPartenariat.EN_ATTENTE);
    }

    @Test
    void shouldGetAllPartenariats() {
        List<Partenariat> list = Arrays.asList(partenariat, new Partenariat());
        when(partenariatRepository.findAll()).thenReturn(list);

        List<Partenariat> result = partenariatService.getAll();

        assertEquals(2, result.size());
        verify(partenariatRepository).findAll();
    }

    @Test
    void shouldGetAcceptedPartenariats() {
        partenariat.setStatut(StatutPartenariat.ACCEPTE);
        when(partenariatRepository.findByStatut(StatutPartenariat.ACCEPTE))
                .thenReturn(Collections.singletonList(partenariat));

        List<Partenariat> result = partenariatService.getAcceptes();

        assertEquals(1, result.size());
        assertEquals(StatutPartenariat.ACCEPTE, result.get(0).getStatut());
        verify(partenariatRepository).findByStatut(StatutPartenariat.ACCEPTE);
    }

    @Test
    void shouldGetPartenariatById() {
        when(partenariatRepository.findById(1L)).thenReturn(Optional.of(partenariat));

        Partenariat result = partenariatService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getIdPartenariat());
        assertEquals("Orange", result.getNomEntreprise());
        verify(partenariatRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenGetByIdNotFound() {
        when(partenariatRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> partenariatService.getById(1L));

        assertEquals("Partenariat not found", ex.getMessage());
        verify(partenariatRepository).findById(1L);
    }

    @Test
    void shouldCreatePartenariatWithStatusEnAttenteAndSendEmails() {
        partenariat.setStatut(StatutPartenariat.ACCEPTE);

        when(partenariatRepository.save(any(Partenariat.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Partenariat result = partenariatService.create(partenariat);

        assertNotNull(result);
        assertEquals(StatutPartenariat.EN_ATTENTE, result.getStatut());

        verify(partenariatRepository).save(partenariat);
        verify(mailService).sendDemandePartenariatToAdmin(partenariat);
        verify(mailService).sendConfirmationToEntreprise(partenariat);
    }

    @Test
    void shouldUpdateStatutAndSendNotification() {
        when(partenariatRepository.findById(1L)).thenReturn(Optional.of(partenariat));
        when(partenariatRepository.save(any(Partenariat.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Partenariat result = partenariatService.updateStatut(1L, StatutPartenariat.ACCEPTE);

        assertNotNull(result);
        assertEquals(StatutPartenariat.ACCEPTE, result.getStatut());

        verify(partenariatRepository).findById(1L);
        verify(partenariatRepository).save(partenariat);
        verify(mailService).sendStatutUpdateToEntreprise(partenariat);
    }

    @Test
    void shouldThrowExceptionWhenUpdateStatutPartenariatNotFound() {
        when(partenariatRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> partenariatService.updateStatut(1L, StatutPartenariat.ACCEPTE));

        assertEquals("Partenariat not found", ex.getMessage());

        verify(partenariatRepository).findById(1L);
        verify(partenariatRepository, never()).save(any());
        verify(mailService, never()).sendStatutUpdateToEntreprise(any());
    }

    @Test
    void shouldDeletePartenariat() {
        doNothing().when(partenariatRepository).deleteById(1L);

        partenariatService.delete(1L);

        verify(partenariatRepository).deleteById(1L);
    }

    @Test
    void shouldFindById() {
        when(partenariatRepository.findById(1L)).thenReturn(Optional.of(partenariat));

        Partenariat result = partenariatService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getIdPartenariat());
        verify(partenariatRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenFindByIdNotFound() {
        when(partenariatRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> partenariatService.findById(2L));

        assertEquals("Partenariat introuvable : 2", ex.getMessage());
        verify(partenariatRepository).findById(2L);
    }
}
