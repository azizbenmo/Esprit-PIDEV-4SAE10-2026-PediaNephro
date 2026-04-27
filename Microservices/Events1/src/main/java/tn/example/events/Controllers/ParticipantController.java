package tn.example.events.Controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.example.events.Entities.Participant;
import tn.example.events.Services.ParticipantService;

import java.util.List;

@RestController
@RequestMapping("/participants")
/*@CrossOrigin(
        origins = "http://localhost:4200",
        allowedHeaders = "*",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH }
)*/
public class ParticipantController {

    private final ParticipantService participantService;

    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Participant> createParticipant(@RequestBody Participant participant) {
        return ResponseEntity.ok(participantService.addParticipant(participant));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Participant> updateParticipant(
            @PathVariable Long id,
            @RequestBody Participant participant) {

        participant.setIdParticipant(id);
        return ResponseEntity.ok(participantService.updateParticipant(participant));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParticipant(@PathVariable Long id) {
        participantService.deleteParticipant(id);
        return ResponseEntity.noContent().build();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Participant> getById(@PathVariable Long id) {
        return participantService.getParticipantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<Participant>> getAll() {
        return ResponseEntity.ok(participantService.getAllParticipants());
    }
}
