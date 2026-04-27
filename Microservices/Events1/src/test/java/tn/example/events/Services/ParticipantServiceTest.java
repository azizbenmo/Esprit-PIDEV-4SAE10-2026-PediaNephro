package tn.example.events.Services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.example.events.Entities.Participant;
import tn.example.events.Repositories.ParticipantRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private ParticipantService participantService;

    private Participant participant;

    @BeforeEach
    void setUp() {
        participant = new Participant();
        participant.setIdParticipant(1L);
        participant.setNom("Amri");
        participant.setPrenom("Oussema");
        participant.setEmail("oussema@test.com");
    }

    @Test
    void shouldAddParticipant() {
        when(participantRepository.save(participant)).thenReturn(participant);

        Participant result = participantService.addParticipant(participant);

        assertNotNull(result);
        assertEquals(1L, result.getIdParticipant());
        assertEquals("Amri", result.getNom());
        verify(participantRepository).save(participant);
    }

    @Test
    void shouldUpdateParticipant() {
        participant.setNom("Ben Ali");

        when(participantRepository.save(participant)).thenReturn(participant);

        Participant result = participantService.updateParticipant(participant);

        assertNotNull(result);
        assertEquals("Ben Ali", result.getNom());
        verify(participantRepository).save(participant);
    }

    @Test
    void shouldDeleteParticipant() {
        doNothing().when(participantRepository).deleteById(1L);

        participantService.deleteParticipant(1L);

        verify(participantRepository).deleteById(1L);
    }

    @Test
    void shouldGetParticipantById() {
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));

        Optional<Participant> result = participantService.getParticipantById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getIdParticipant());
        assertEquals("Oussema", result.get().getPrenom());
        verify(participantRepository).findById(1L);
    }

    @Test
    void shouldReturnEmptyWhenParticipantNotFound() {
        when(participantRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Participant> result = participantService.getParticipantById(99L);

        assertFalse(result.isPresent());
        verify(participantRepository).findById(99L);
    }

    @Test
    void shouldGetAllParticipants() {
        Participant p2 = new Participant();
        p2.setIdParticipant(2L);
        p2.setNom("Sarra");
        p2.setPrenom("Ali");
        p2.setEmail("sarra@test.com");

        List<Participant> participants = Arrays.asList(participant, p2);

        when(participantRepository.findAll()).thenReturn(participants);

        List<Participant> result = participantService.getAllParticipants();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(participantRepository).findAll();
    }
}
