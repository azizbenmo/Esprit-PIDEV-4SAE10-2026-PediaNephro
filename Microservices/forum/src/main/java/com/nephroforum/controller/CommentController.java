package com.nephroforum.controller;

import com.nephroforum.dto.CommentDTOs;
import com.nephroforum.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<List<CommentDTOs.CommentResponse>> list(
            @PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<CommentDTOs.CommentResponse> add(
            @PathVariable Long postId,
            @RequestBody CommentDTOs.CreateCommentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(
                        postId, req.content(), req.authorName(),
                        req.parentId(), req.anonymous()));
    }

    @PutMapping("/api/posts/{postId}/comments/{commentId}")
    public ResponseEntity<CommentDTOs.CommentResponse> update(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentDTOs.UpdateCommentRequest req) {
        return ResponseEntity.ok(commentService.updateComment(commentId, req.content()));
    }

    @DeleteMapping("/api/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/posts/{postId}/comments/{commentId}/official")
    public ResponseEntity<CommentDTOs.CommentResponse> markOfficial(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.markAsOfficial(postId, commentId));
    }

    // ← Route séparée sans {postId} pour éviter le conflit
    @GetMapping("/api/comments/my/{authorName}")
    public ResponseEntity<List<CommentDTOs.CommentResponse>> getMyComments(
            @PathVariable String authorName) {
        return ResponseEntity.ok(commentService.getMyComments(authorName));
    }
}