package tn.example.events.Services;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.example.events.Entities.Event;
import tn.example.events.Entities.Inscription;
import tn.example.events.Entities.Partenariat;
import tn.example.events.Entities.Statut;
import tn.example.events.Repositories.EventRepository;
import tn.example.events.Repositories.InscriptionRepository;
import tn.example.events.Repositories.PartenariatRepository;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    @Autowired
    private PartenariatRepository partenariatRepository;

    @Autowired
    private InscriptionRepository inscriptionRepository;

    @Autowired
    private MailService mailService;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // Ajouter un événement
    public Event addEvent(Event event) {
        event.setArchive(false); // ✅ Par défaut non archivé
        return eventRepository.save(event);
    }

    // Modifier un événement
    public Event updateEvent(Event event) {
        return eventRepository.save(event);
    }

    // ✅ ARCHIVER (au lieu de supprimer)
    public Event archiverEvent(Long idEvent) {
        Event event = eventRepository.findById(idEvent)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));
        event.setArchive(true);
        return eventRepository.save(event);
    }

    // ✅ RESTAURER un événement archivé
    public Event restaurerEvent(Long idEvent) {
        Event event = eventRepository.findById(idEvent)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));
        event.setArchive(false);
        return eventRepository.save(event);
    }

    // ✅ Supprimer définitivement (garde si besoin admin)
    public void deleteEvent(Long idEvent) {
        eventRepository.deleteById(idEvent);
    }

    // Récupérer un événement par ID
    public Optional<Event> getEventById(Long idEvent) {
        return eventRepository.findById(idEvent);
    }

    public Event getById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement not found avec id: " + id));
    }

    //  Récupérer UNIQUEMENT les événements NON archivés
    public List<Event> getAllEvents() {
        return eventRepository.findByArchiveFalse();
    }

    //  Récupérer les événements archivés
    public List<Event> getArchivedEvents() {
        return eventRepository.findByArchiveTrue();
    }

    //  Récupérer TOUS (archivés + non archivés) - optionnel
    public List<Event> getAllEventsIncludingArchived() {
        return eventRepository.findAll();
    }

    //partenariat
    public Event assignPartenaire(Long eventId, Long partenariatId) {
        Event event = getById(eventId);
        Partenariat partenariat = partenariatRepository.findById(partenariatId)
                .orElseThrow(() -> new RuntimeException("Partenariat not found"));
        event.setPartenariat(partenariat);
        return eventRepository.save(event);
    }

    public Event removePartenaire(Long eventId) {
        Event event = getById(eventId);
        event.setPartenariat(null);
        return eventRepository.save(event);
    }


    // ✅ Envoie certificats à tous les CONFIRME quand événement terminé
    public void envoyerCertificats(Long eventId) {
        Event event = getById(eventId);

        List<Inscription> confirmes = inscriptionRepository
                .findByEventAndStatut(event, Statut.CONFIRME);

        confirmes.forEach(inscription -> {
            mailService.sendCertificatParticipation(inscription);
        });
    }

}
