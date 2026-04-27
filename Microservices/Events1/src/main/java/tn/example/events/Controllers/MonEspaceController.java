package tn.example.events.Controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.example.events.Services.MonEspaceService;
import tn.example.events.dto.MonEspaceDTO;

@RestController
@RequestMapping("/participants")
@RequiredArgsConstructor
public class MonEspaceController {

    private final MonEspaceService monEspaceService;

    @GetMapping("/mon-espace")
    public ResponseEntity<?> getMonEspace(@RequestParam String email) {
        try {
            MonEspaceDTO data = monEspaceService.getMonEspace(email);
            return ResponseEntity.ok(data);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body("Aucun participant trouvé avec cet email : " + email);
        }
    }
}
