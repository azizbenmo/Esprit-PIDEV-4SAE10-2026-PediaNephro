package tn.example.events.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.example.events.Entities.Inscription;
import tn.example.events.Entities.Statut;
import tn.example.events.Entities.TypeParticipant;
import tn.example.events.Repositories.InscriptionRepository;
import tn.example.events.Services.InscriptionService;
import tn.example.events.dto.CreateInscriptionFromUserDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inscriptions")
/*@CrossOrigin(
        origins = "http://localhost:4200",
        allowedHeaders = "*",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH }
)*/
public class InscriptionController {

    private final InscriptionService inscriptionService;

    public InscriptionController(InscriptionService inscriptionService) {
        this.inscriptionService = inscriptionService;
    }

    @Autowired
    private InscriptionRepository inscriptionRepository;

    // ✅ CREATE admin
    @PostMapping("/addInscrip")
    public ResponseEntity<Inscription> createInscription(
            @RequestParam Long eventId,
            @RequestParam Long participantId,
            @RequestParam Statut statut,
            @RequestParam TypeParticipant typeParticipant) {
        return ResponseEntity.ok(
                inscriptionService.addInscription(eventId, participantId, statut, typeParticipant)
        );
    }

    // ✅ CREATE front avec liste d'attente
    @PostMapping("/front")
    public ResponseEntity<?> createFromFront(
            @RequestParam Long eventId,
            @RequestParam Long participantId,
            @RequestParam TypeParticipant typeParticipant) {
        try {
            return ResponseEntity.ok(
                    inscriptionService.create(eventId, participantId, typeParticipant)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/front/by-user")
    public ResponseEntity<?> createFromFrontByUser(@RequestBody CreateInscriptionFromUserDto body) {
        try {
            return ResponseEntity.ok(inscriptionService.createFromUserAccount(body));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Inscription> update(
            @PathVariable Long id,
            @RequestBody Inscription inscription) {
        inscription.setIdInscription(id);
        return ResponseEntity.ok(inscriptionService.updateInscription(inscription));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inscriptionService.deleteInscription(id);
        return ResponseEntity.noContent().build();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Inscription> getById(@PathVariable Long id) {
        return inscriptionService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<Inscription>> getAll() {
        return ResponseEntity.ok(inscriptionService.getAll());
    }

    // UPDATE STATUT
    @PatchMapping("/{id}/statut")
    public ResponseEntity<Inscription> updateStatut(
            @PathVariable Long id,
            @RequestParam Statut statut) {
        return ResponseEntity.ok(inscriptionService.updateStatut(id, statut));
    }

    @GetMapping("/heatmap")
    public ResponseEntity<List<Map<String, Object>>> getHeatmap() {
        List<Inscription> inscriptions = inscriptionService.getAll();

        Map<String, Long> countMap = inscriptions.stream()
                .filter(i -> i.getDateInscription() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getDateInscription().getDayOfWeek().getValue() + "-" +
                                i.getDateInscription().getHour(),
                        Collectors.counting()
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : countMap.entrySet()) {
            String[] parts = entry.getKey().split("-");
            Map<String, Object> item = new HashMap<>();
            item.put("day", Integer.parseInt(parts[0]));
            item.put("hour", Integer.parseInt(parts[1]));
            item.put("count", entry.getValue());
            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/filter")
    @CrossOrigin("http://localhost:4200")
    public ResponseEntity<List<Inscription>> filterInscriptions(
            @RequestParam(required = false) Long idEvent,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin) {

        System.out.println("=== FILTER ===");
        System.out.println("idEvent: " + idEvent);
        System.out.println("statut: " + statut);
        System.out.println("type: " + type);
        System.out.println("search: " + search);
        System.out.println("dateDebut: " + dateDebut);
        System.out.println("dateFin: " + dateFin);

        List<Inscription> result = inscriptionService.filterInscriptions(
                idEvent, statut, type, search, dateDebut, dateFin
        );

        System.out.println("Résultats: " + result.size());
        return ResponseEntity.ok(result);
    }
}