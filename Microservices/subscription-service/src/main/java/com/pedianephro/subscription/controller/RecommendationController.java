package com.pedianephro.subscription.controller;

import com.pedianephro.subscription.dto.PatientProfileRequest;
import com.pedianephro.subscription.dto.RecommendationResponse;
import com.pedianephro.subscription.entity.PatientProfile;
import com.pedianephro.subscription.repository.PatientProfileRepository;
import com.pedianephro.subscription.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/recommendations", "/recommendations"})
@CrossOrigin
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final PatientProfileRepository patientProfileRepository;

    @PostMapping("/suggest")
    public ResponseEntity<RecommendationResponse> suggest(@Valid @RequestBody PatientProfileRequest request) {
        return ResponseEntity.ok(recommendationService.recommend(request));
    }

    @PostMapping("/save-profile")
    public ResponseEntity<RecommendationResponse> saveProfile(@Valid @RequestBody PatientProfileRequest request) {
        return ResponseEntity.ok(recommendationService.saveProfileAndRecommend(request));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<PatientProfile> getProfile(@PathVariable Long userId) {
        return patientProfileRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
