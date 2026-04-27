package com.nephroforum.controller;

import com.nephroforum.entity.BanAppeal;
import com.nephroforum.service.BanAppealService;
import com.nephroforum.service.BanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ban")
@RequiredArgsConstructor
public class BanController {

    private final BanService banService;
    private final BanAppealService appealService;

    @GetMapping("/status/{username}")
    public ResponseEntity<Map<String, Object>> status(@PathVariable String username) {
        return ResponseEntity.ok(banService.getBanStatus(username));
    }

    @PostMapping("/violation/{username}")
    public ResponseEntity<Map<String, Object>> addViolation(@PathVariable String username) {
        return ResponseEntity.ok(banService.addViolation(username));
    }

    @DeleteMapping("/reset/{username}")
    public ResponseEntity<Void> reset(@PathVariable String username) {
        banService.resetBan(username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin-ban/{username}")
    public ResponseEntity<Map<String, Object>> adminBan(@PathVariable String username) {
        return ResponseEntity.ok(banService.adminBan(username));
    }

    // ── Réclamations ──────────────────────────────────────────────────────────
    @PostMapping("/appeal/{username}")
    public ResponseEntity<BanAppeal> createAppeal(
            @PathVariable String username,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                appealService.createAppeal(username, body.get("reason"))
        );
    }

    @GetMapping("/appeals/pending")
    public ResponseEntity<List<BanAppeal>> getPendingAppeals() {
        return ResponseEntity.ok(appealService.getPendingAppeals());
    }

    @PutMapping("/appeals/{appealId}/accept")
    public ResponseEntity<Void> acceptAppeal(@PathVariable Long appealId) {
        appealService.acceptAppeal(appealId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/appeals/{appealId}/reject")
    public ResponseEntity<Void> rejectAppeal(@PathVariable Long appealId) {
        appealService.rejectAppeal(appealId);
        return ResponseEntity.ok().build();
    }
}