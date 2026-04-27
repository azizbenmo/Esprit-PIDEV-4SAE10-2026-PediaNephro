package tn.example.events.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.example.events.Entities.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    //  Trouver les événements NON archivés
    List<Event> findByArchiveFalse();

    //  Trouver les événements archivés
    List<Event> findByArchiveTrue();
}
