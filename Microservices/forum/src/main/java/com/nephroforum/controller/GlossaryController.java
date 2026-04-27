package com.nephroforum.controller;

import com.nephroforum.service.GlossaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/glossary")
@RequiredArgsConstructor
public class GlossaryController {

    private final GlossaryService glossaryService;

    @PostMapping("/detect")
    public ResponseEntity<List<Map<String, String>>> detect(@RequestBody String text) {
        return ResponseEntity.ok(glossaryService.detectWithAI(text));
    }
}