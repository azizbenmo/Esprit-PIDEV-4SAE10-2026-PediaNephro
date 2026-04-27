package com.nephroforum.controller;

import com.nephroforum.dto.AIDTOs;
import com.nephroforum.service.AIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/generate-description")
    public ResponseEntity<AIDTOs.DescriptionResponse> generateDesc(
            @Valid @RequestBody AIDTOs.DescriptionRequest req) {
        return ResponseEntity.ok(
                new AIDTOs.DescriptionResponse(aiService.generateDescription(req.title())));
    }

    @PostMapping("/generate-tags")
    public ResponseEntity<AIDTOs.TagsResponse> generateTags(
            @Valid @RequestBody AIDTOs.TagsRequest req) {
        return ResponseEntity.ok(
                new AIDTOs.TagsResponse(aiService.generateTags(req.title(), req.description())));
    }

    @PostMapping("/translate")
    public ResponseEntity<AIDTOs.TranslationResponse> translate(
            @Valid @RequestBody AIDTOs.TranslationRequest req) {
        return ResponseEntity.ok(
                new AIDTOs.TranslationResponse(aiService.translate(req.text(), req.targetLang())));
    }
}
