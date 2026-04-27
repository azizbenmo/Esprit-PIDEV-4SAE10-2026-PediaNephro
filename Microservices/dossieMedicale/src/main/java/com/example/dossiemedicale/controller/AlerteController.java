package com.example.dossiemedicale.controller;

import com.example.dossiemedicale.DTO.AlerteRequest;
import com.example.dossiemedicale.entities.Alerte;
import com.example.dossiemedicale.services.AlerteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alertes")
@RequiredArgsConstructor
public class AlerteController {

    private final AlerteService alerteService;

    @PostMapping("/ajouter")
    public ResponseEntity<Alerte> ajouter(@RequestBody AlerteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(alerteService.ajouterAlerte(request));
    }

    @PutMapping("/modifier/{id}")
    public ResponseEntity<Alerte> modifier(@PathVariable Long id, @RequestBody AlerteRequest request) {
        return ResponseEntity.ok(alerteService.modifierAlerte(id, request));
    }

    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        alerteService.supprimerAlerte(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<Alerte>> all() {
        return ResponseEntity.ok(alerteService.getAllAlertes());
    }

    @GetMapping("/diagnostic")
    public ResponseEntity<Map<String, Object>> diagnostic() {
        Map<String, Object> diag = new HashMap<>();
        try {
            diag.put("alertes_count", alerteService.getAllAlertes().size());
            diag.put("alertes", alerteService.getAllAlertes());
        } catch (Exception e) {
            diag.put("alertes_error", e.getMessage());
        }
        return ResponseEntity.ok(diag);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alerte> get(@PathVariable Long id) {
        return ResponseEntity.ok(alerteService.getAlerteById(id));
    }
    @GetMapping("/dossier/{dossierId}")
    public List<Alerte> getByDossier(@PathVariable Long dossierId) {
        return alerteService.getAlertesByDossier(dossierId);
    }

}