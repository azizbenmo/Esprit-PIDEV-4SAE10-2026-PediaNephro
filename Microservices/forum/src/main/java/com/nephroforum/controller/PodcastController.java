package com.nephroforum.controller;

import com.nephroforum.dto.PodcastDTOs;
import com.nephroforum.service.PodcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/podcasts")
@RequiredArgsConstructor
public class PodcastController {

    private final PodcastService podcastService;

    @GetMapping
    public ResponseEntity<List<PodcastDTOs.PodcastResponse>> getAll() {
        return ResponseEntity.ok(podcastService.getAll());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<PodcastDTOs.PodcastResponse>> getByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(podcastService.getByCategory(category));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PodcastDTOs.PodcastResponse>> search(
            @RequestParam String keyword) {
        return ResponseEntity.ok(podcastService.search(keyword));
    }
    @GetMapping("/author/{authorName}")
    public ResponseEntity<List<PodcastDTOs.PodcastResponse>> getByAuthor(
            @PathVariable String authorName) {
        return ResponseEntity.ok(podcastService.getByAuthor(authorName));
    }

    @PostMapping
    public ResponseEntity<PodcastDTOs.PodcastResponse> create(
            @RequestBody PodcastDTOs.CreatePodcastRequest req) {
        return ResponseEntity.ok(podcastService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PodcastDTOs.PodcastResponse> update(
            @PathVariable Long id,
            @RequestBody PodcastDTOs.UpdatePodcastRequest req) {
        return ResponseEntity.ok(podcastService.update(id, req));
    }

    @PostMapping("/{id}/play")
    public ResponseEntity<Void> play(@PathVariable Long id) {
        podcastService.incrementPlays(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        podcastService.delete(id);
        return ResponseEntity.ok().build();
    }
}