package com.nephroforum.controller;

import com.nephroforum.dto.PostDTOs;
import com.nephroforum.dto.SearchDTOs;
import com.nephroforum.entity.Post;
import com.nephroforum.exception.ResourceNotFoundException;
import com.nephroforum.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor

public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<Page<PostDTOs.PostResponse>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "RECENT") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getPosts(keyword, sort, page, size));
    }

    @PostMapping("/advanced-search")
    public ResponseEntity<Page<PostDTOs.PostResponse>> advancedSearch(
            @RequestBody SearchDTOs.SearchRequest req) {
        return ResponseEntity.ok(postService.advancedSearch(req));
    }

    @PostMapping
    public ResponseEntity<PostDTOs.PostResponse> create(
            @RequestBody PostDTOs.CreatePostRequest req) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(
                        req.title(), req.description(),
                        req.tags(), req.authorName(),
                        req.anonymous(), null));
    }

    @PostMapping(value = "/{id}/upload-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDTOs.PostResponse> uploadImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) throws IOException {
        return ResponseEntity.ok(postService.uploadImage(id, image));
    }

    // Endpoint unique pour getPost avec viewerName optionnel
    @GetMapping("/{id}")
    public ResponseEntity<PostDTOs.PostResponse> getPost(
            @PathVariable Long id,
            @RequestParam(required = false) String viewerName) {
        return ResponseEntity.ok(postService.getPost(id, viewerName));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDTOs.PostResponse> update(
            @PathVariable Long id,
            @RequestBody PostDTOs.UpdatePostRequest req) {
        return ResponseEntity.ok(
                postService.updatePost(id, req.title(), req.description(), req.tags()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/author/{authorName}")
    public ResponseEntity<List<PostDTOs.PostResponse>> getByAuthor(
            @PathVariable String authorName,
            @RequestParam Long excludeId) {
        return ResponseEntity.ok(postService.getPostsByAuthor(authorName, excludeId));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(postService.getStats());
    }


    @GetMapping("/{id}/summarize")
    public ResponseEntity<Map<String, String>> summarize(@PathVariable Long id) {
        return ResponseEntity.ok(postService.summarizePost(id));
    }

    @PatchMapping("/{id}/pin")
    public ResponseEntity<PostDTOs.PostResponse> pinPost(
            @PathVariable Long id,
            @RequestParam String doctorName,
            @RequestParam boolean pin) {
        return ResponseEntity.ok(postService.pinPost(id, doctorName, pin));
    }
}