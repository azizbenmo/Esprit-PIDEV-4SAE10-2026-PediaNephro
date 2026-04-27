package tn.example.events.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.example.events.Entities.Participant;

import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    Optional<Participant> findByEmail(String email);
}
