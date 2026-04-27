package com.nephroforum.controller;

import com.nephroforum.dto.BadgeDTOs;
import com.nephroforum.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping("/{ownerName}")
    public ResponseEntity<List<BadgeDTOs.BadgeResponse>> getBadges(
            @PathVariable String ownerName) {
        return ResponseEntity.ok(badgeService.getBadges(ownerName));
    }

    @GetMapping
    public ResponseEntity<List<BadgeDTOs.BadgeResponse>> getAllBadges() {
        return ResponseEntity.ok(badgeService.getAllBadges());
    }

    @PostMapping("/best-answer/{commentId}")
    public ResponseEntity<Void> awardBestAnswer(
            @PathVariable Long commentId,
            @RequestParam String awardedBy) {
        badgeService.awardBestAnswer(commentId, awardedBy);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/award")
    public ResponseEntity<Void> awardBadge(
            @RequestBody BadgeDTOs.AwardBadgeRequest req) {
        badgeService.awardBadgeManually(req.ownerName(), req.type(), req.awardedBy());
        return ResponseEntity.noContent().build();
    }
}