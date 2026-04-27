package com.nephroforum.controller;

import com.nephroforum.dto.ForumProfileDTOs;
import com.nephroforum.service.ForumProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ForumProfileController {

    private final ForumProfileService profileService;

    @GetMapping("/{username}")
    public ResponseEntity<ForumProfileDTOs.ProfileResponse> get(
            @PathVariable String username) {
        return ResponseEntity.ok(profileService.getOrCreate(username));
    }

    @PutMapping("/{username}")
    public ResponseEntity<ForumProfileDTOs.ProfileResponse> update(
            @PathVariable String username,
            @RequestBody ForumProfileDTOs.UpsertProfileRequest req) {
        return ResponseEntity.ok(profileService.upsert(req));
    }
}