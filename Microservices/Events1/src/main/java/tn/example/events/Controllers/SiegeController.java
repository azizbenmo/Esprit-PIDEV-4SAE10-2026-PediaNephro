package tn.example.events.Controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.example.events.Entities.Siege;
import tn.example.events.Services.SiegeService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sieges")
@RequiredArgsConstructor
public class SiegeController {

    private final SiegeService siegeService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Siege>> getSieges(@PathVariable Long eventId) {
        return ResponseEntity.ok(siegeService.getSiegesParEvent(eventId));
    }

    @PostMapping("/{siegeId}/reserver")
    public ResponseEntity<Siege> reserver(
            @PathVariable Long siegeId,
            @RequestBody Map<String, Long> body) {
        Long inscriptionId = body.get("inscriptionId");
        return ResponseEntity.ok(siegeService.reserverSiege(siegeId, inscriptionId));
    }
}
