package com.nephroforum.controller;

import com.nephroforum.dto.ReactionDTOs;
import com.nephroforum.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping
    public ResponseEntity<Boolean> toggle(
            @RequestBody ReactionDTOs.ReactionRequest req) {
        return ResponseEntity.ok(
                reactionService.toggle(req.postId(), req.commentId(), req.reactionType()));
    }
}