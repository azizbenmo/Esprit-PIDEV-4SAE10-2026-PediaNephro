package tn.example.events.Controllers;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.example.events.Entities.Event;
import tn.example.events.Entities.Inscription;
import tn.example.events.Services.*;
import tn.example.events.dto.PubMedPublication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
/*@CrossOrigin(
        origins = "http://localhost:4200",
        allowedHeaders = "*",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH })*/
public class EventController {

    private final EventService eventService;

    @Autowired
    private PredictionService predictionService;

    @Autowired
    private CertificatService certificatService;

    @Autowired
    private InscriptionService inscriptionService;

    @Autowired
    private PubMedService pubMedService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // ✅ Créer un événement
    @PostMapping("/addEvent")
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        try {
            Event savedEvent = eventService.addEvent(event);
            return ResponseEntity.ok(savedEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Modifier un événement
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestBody Event event) {
        event.setIdEvent(id);
        Event updatedEvent = eventService.updateEvent(event);
        return ResponseEntity.ok(updatedEvent);
    }

    // ✅ ARCHIVER (remplace la suppression)
    @PatchMapping("/{id}/archiver")
    public ResponseEntity<Event> archiverEvent(@PathVariable Long id) {
        Event archivedEvent = eventService.archiverEvent(id);
        return ResponseEntity.ok(archivedEvent);
    }

    // ✅ RESTAURER
    @PatchMapping("/{id}/restaurer")
    public ResponseEntity<Event> restaurerEvent(@PathVariable Long id) {
        Event restoredEvent = eventService.restaurerEvent(id);
        return ResponseEntity.ok(restoredEvent);
    }

    // ✅ Supprimer définitivement (optionnel - pour admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // Récupérer un événement par ID
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Récupérer les événements NON archivés
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    // ✅ Récupérer les événements archivés
    @GetMapping("/archives")
    public ResponseEntity<List<Event>> getArchivedEvents() {
        return ResponseEntity.ok(eventService.getArchivedEvents());
    }

    // ✅ Endpoint pour vérifier les places restantes
    @GetMapping("/{id}/places")
    public ResponseEntity<Map<String, Object>> getPlaces(@PathVariable Long id) {
        Event event = eventService.getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("capacite", event.getCapacite());
        result.put("placesRestantes", event.getPlacesRestantes());
        result.put("complet", event.getPlacesRestantes() <= 0);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{eventId}/partenaire/{partenariatId}")
    public ResponseEntity<Event> assignPartenaire(
            @PathVariable Long eventId,
            @PathVariable Long partenariatId) {
        return ResponseEntity.ok(eventService.assignPartenaire(eventId, partenariatId));
    }

    @DeleteMapping("/{eventId}/partenaire")
    public ResponseEntity<Event> removePartenaire(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.removePartenaire(eventId));
    }

    @PostMapping("/{id}/envoyer-certificats")
    public ResponseEntity<Map<String, Object>> envoyerCertificats(
            @PathVariable Long id) {

        Event event = eventService.getById(id);

        // ✅ Vérifie que l'événement est bien terminé
        if (event.getDateFin().isAfter(java.time.LocalDateTime.now())) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "L'événement n'est pas encore terminé");
            return ResponseEntity.badRequest().body(error);
        }

        eventService.envoyerCertificats(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Certificats envoyés avec succès");
        return ResponseEntity.ok(response);
    }

    // ✅ Télécharger un certificat individuel
    @GetMapping("/{eventId}/certificat/{inscriptionId}")
    public ResponseEntity<byte[]> downloadCertificat(
            @PathVariable Long eventId,
            @PathVariable Long inscriptionId) {
        try {
            Inscription inscription = inscriptionService.getById(inscriptionId)
                    .orElseThrow(() -> new RuntimeException("Not found"));

            byte[] pdf = certificatService.generateCertificat(inscription);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition",
                            "attachment; filename=Certificat_" +
                                    inscription.getParticipant().getNom() + ".pdf")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // prediction endpoint
    @GetMapping("/{id}/prediction")
    public ResponseEntity<PredictionService.PredictionResult> getPrediction(@PathVariable Long id) {
        Event event = eventService.getById(id);
        PredictionService.PredictionResult result = predictionService.predict(event);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/{id}/publications")
    public ResponseEntity<List<PubMedPublication>> getPublications(
            @PathVariable Long id) {
        Event event = eventService.getById(id);
        List<PubMedPublication> publications =
                pubMedService.rechercherPublications(
                        event.getNomEvent(),
                        event.getDescription()
                );
        return ResponseEntity.ok(publications);
    }
}