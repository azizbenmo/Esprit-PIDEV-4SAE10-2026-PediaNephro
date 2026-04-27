package com.nephroforum.controller;

import com.nephroforum.dto.PollDTOs;
import com.nephroforum.service.PollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;

    @GetMapping
    public ResponseEntity<List<PollDTOs.PollResponse>> getActive(
            @RequestParam String voterName) {
        return ResponseEntity.ok(pollService.getActivePolls(voterName));
    }

    @PostMapping
    public ResponseEntity<PollDTOs.PollResponse> create(
            @RequestBody PollDTOs.CreatePollRequest req) {
        return ResponseEntity.ok(pollService.createPoll(req));
    }

    @PostMapping("/{pollId}/vote")
    public ResponseEntity<PollDTOs.PollResponse> vote(
            @PathVariable Long pollId,
            @RequestParam Long optionId,
            @RequestParam String voterName) {
        return ResponseEntity.ok(pollService.vote(pollId, optionId, voterName));
    }

    @PutMapping("/{pollId}")
    public ResponseEntity<PollDTOs.PollResponse> updatePoll(
            @PathVariable Long pollId,
            @RequestBody PollDTOs.UpdatePollRequest req) {
        return ResponseEntity.ok(pollService.updatePoll(pollId, req));
    }

    @PutMapping("/{pollId}/close")
    public ResponseEntity<Void> closePoll(@PathVariable Long pollId) {
        pollService.closePoll(pollId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/archived")
    public ResponseEntity<List<PollDTOs.PollResponse>> getArchived() {
        return ResponseEntity.ok(pollService.getArchivedPolls());
    }
}
