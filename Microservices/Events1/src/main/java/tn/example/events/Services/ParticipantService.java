package tn.example.events.Services;


import org.springframework.stereotype.Service;
import tn.example.events.Entities.Participant;
import tn.example.events.Repositories.ParticipantRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    public ParticipantService(ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    public Participant addParticipant(Participant participant) {
        return participantRepository.save(participant);
    }

    public Participant updateParticipant(Participant participant) {
        return participantRepository.save(participant);
    }

    public void deleteParticipant(Long id) {
        participantRepository.deleteById(id);
    }

    public Optional<Participant> getParticipantById(Long id) {
        return participantRepository.findById(id);
    }

    public List<Participant> getAllParticipants() {
        return participantRepository.findAll();
    }
}