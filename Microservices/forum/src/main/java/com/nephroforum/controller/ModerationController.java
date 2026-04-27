package com.nephroforum.controller;

import com.nephroforum.service.ModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/moderation")
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationService moderationService;

    // Bad Words
    @GetMapping("/bad-words")
    public ResponseEntity<List<String>> getBadWords() {
        return ResponseEntity.ok(moderationService.getBadWords());
    }

    @PostMapping("/bad-words")
    public ResponseEntity<Void> addBadWord(@RequestBody Map<String, String> body) {
        moderationService.addBadWord(body.get("word"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/bad-words/{word}")
    public ResponseEntity<Void> removeBadWord(@PathVariable String word) {
        moderationService.removeBadWord(word);
        return ResponseEntity.ok().build();
    }

    // Medicines
    @GetMapping("/medicines")
    public ResponseEntity<List<String>> getMedicines() {
        return ResponseEntity.ok(moderationService.getMedicines());
    }

    @PostMapping("/medicines")
    public ResponseEntity<Void> addMedicine(@RequestBody Map<String, String> body) {
        moderationService.addMedicine(body.get("word"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/medicines/{medicine}")
    public ResponseEntity<Void> removeMedicine(@PathVariable String medicine) {
        moderationService.removeMedicine(medicine);
        return ResponseEntity.ok().build();
    }
}