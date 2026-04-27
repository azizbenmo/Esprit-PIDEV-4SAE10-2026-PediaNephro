package com.nephroforum.controller;

import com.nephroforum.service.FollowUpScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final FollowUpScheduler followUpScheduler;

    // Déclencher manuellement pour tester
    @PostMapping("/check-unanswered")
    public ResponseEntity<String> triggerCheck() {
        followUpScheduler.checkUnansweredPosts();
        return ResponseEntity.ok("✅ Vérification déclenchée !");
    }
}