package tn.example.events.Controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.example.events.Entities.Partenariat;
import tn.example.events.Entities.StatutPartenariat;
import tn.example.events.Services.PartenariatService;
import java.util.List;

@RestController
@RequestMapping("/partenariats")
@RequiredArgsConstructor
/*@CrossOrigin(
        origins = "http://localhost:4200",
        allowedHeaders = "*",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH }
)*/
public class PartenariatController {
    private final PartenariatService partenariatService;

    // Tous les partenariats (admin)
    @GetMapping
    public List<Partenariat> getAll() {
        return partenariatService.getAll();
    }

    // Partenaires acceptés (front office)
    @GetMapping("/acceptes")
    public List<Partenariat> getAcceptes() {
        return partenariatService.getAcceptes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Partenariat> getById(@PathVariable Long id) {
        return ResponseEntity.ok(partenariatService.getById(id));
    }

    // Demande de partenariat (front office)
    @PostMapping("/demande")
    public ResponseEntity<Partenariat> create(@RequestBody Partenariat partenariat) {
        return ResponseEntity.ok(partenariatService.create(partenariat));
    }

    // Mise à jour statut (admin)
    @PatchMapping("/{id}/statut")
    public ResponseEntity<Partenariat> updateStatut(
            @PathVariable Long id,
            @RequestParam StatutPartenariat statut) {
        return ResponseEntity.ok(partenariatService.updateStatut(id, statut));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        partenariatService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
