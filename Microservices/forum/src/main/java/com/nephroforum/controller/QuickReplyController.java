package com.nephroforum.controller;

import com.nephroforum.dto.QuickReplyDTOs;
import com.nephroforum.service.QuickReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quick-replies")
@RequiredArgsConstructor
public class QuickReplyController {

    private final QuickReplyService quickReplyService;

    // Voir les réponses rapides d'un médecin
    @GetMapping("/{ownerName}")
    public ResponseEntity<List<QuickReplyDTOs.QuickReplyResponse>> getByOwner(
            @PathVariable String ownerName) {
        return ResponseEntity.ok(quickReplyService.getByOwner(ownerName));
    }

    // Créer une nouvelle réponse rapide
    @PostMapping
    public ResponseEntity<QuickReplyDTOs.QuickReplyResponse> create(
            @RequestBody QuickReplyDTOs.CreateQuickReplyRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quickReplyService.create(req));
    }

    // Supprimer une réponse rapide
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quickReplyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
