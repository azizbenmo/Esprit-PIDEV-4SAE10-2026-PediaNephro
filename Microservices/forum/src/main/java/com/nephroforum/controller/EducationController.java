package com.nephroforum.controller;

import com.nephroforum.dto.EducationDTOs;
import com.nephroforum.entity.EducationArticle;
import com.nephroforum.service.EducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/education")
@RequiredArgsConstructor
public class EducationController {

    private final EducationService educationService;

    @GetMapping
    public ResponseEntity<List<EducationDTOs.ArticleResponse>> getAll() {
        return ResponseEntity.ok(educationService.getAll());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<EducationDTOs.ArticleResponse>> getByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(educationService.getByCategory(category));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<EducationDTOs.ArticleResponse>> getByType(
            @PathVariable EducationArticle.ArticleType type) {
        return ResponseEntity.ok(educationService.getByType(type));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EducationDTOs.ArticleResponse>> search(
            @RequestParam String keyword) {
        return ResponseEntity.ok(educationService.search(keyword));
    }

    @GetMapping("/author/{authorName}")
    public ResponseEntity<List<EducationDTOs.ArticleResponse>> getByAuthor(
            @PathVariable String authorName) {
        return ResponseEntity.ok(educationService.getByAuthor(authorName));
    }

    @PostMapping
    public ResponseEntity<EducationDTOs.ArticleResponse> create(
            @RequestBody EducationDTOs.CreateArticleRequest req) {
        return ResponseEntity.ok(educationService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EducationDTOs.ArticleResponse> update(
            @PathVariable Long id,
            @RequestBody EducationDTOs.UpdateArticleRequest req) {
        return ResponseEntity.ok(educationService.update(id, req));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> view(@PathVariable Long id) {
        educationService.incrementViews(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        educationService.delete(id);
        return ResponseEntity.ok().build();
    }
}