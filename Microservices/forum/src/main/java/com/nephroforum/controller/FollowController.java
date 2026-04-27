package com.nephroforum.controller;

import com.nephroforum.dto.FollowDTOs;
import com.nephroforum.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    // Envoyer une demande
    @PostMapping("/request")
    public ResponseEntity<FollowDTOs.FollowResponse> sendRequest(
            @RequestBody FollowDTOs.FollowRequest req) {
        return ResponseEntity.ok(
                followService.sendRequest(req.followerName(), req.followingName()));
    }

    // Accepter ou rejeter
    @PutMapping("/{followId}/respond")
    public ResponseEntity<FollowDTOs.FollowResponse> respond(
            @PathVariable Long followId,
            @RequestBody FollowDTOs.RespondRequest req) {
        return ResponseEntity.ok(
                followService.respond(followId, req.responderName(), req.accept()));
    }

    // Se désabonner
    @DeleteMapping("/unfollow")
    public ResponseEntity<Void> unfollow(
            @RequestParam String followerName,
            @RequestParam String followingName) {
        followService.unfollow(followerName, followingName);
        return ResponseEntity.noContent().build();
    }

    // Demandes reçues en attente
    @GetMapping("/{username}/pending")
    public ResponseEntity<List<FollowDTOs.FollowResponse>> pending(
            @PathVariable String username) {
        return ResponseEntity.ok(followService.getPendingRequests(username));
    }

    // Followers
    @GetMapping("/{username}/followers")
    public ResponseEntity<List<FollowDTOs.FollowResponse>> followers(
            @PathVariable String username) {
        return ResponseEntity.ok(followService.getFollowers(username));
    }

    // Following
    @GetMapping("/{username}/following")
    public ResponseEntity<List<FollowDTOs.FollowResponse>> following(
            @PathVariable String username) {
        return ResponseEntity.ok(followService.getFollowing(username));
    }

    // Statut entre deux users
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status(
            @RequestParam String followerName,
            @RequestParam String followingName) {
        return ResponseEntity.ok(Map.of(
                "status", followService.getStatus(followerName, followingName)));
    }

    // Compteurs
    @GetMapping("/{username}/counts")
    public ResponseEntity<Map<String, Long>> counts(
            @PathVariable String username) {
        return ResponseEntity.ok(Map.of(
                "followers", followService.countFollowers(username),
                "following", followService.countFollowing(username)
        ));
    }
    @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelRequest(
            @RequestParam String followerName,
            @RequestParam String followingName) {
        followService.cancelRequest(followerName, followingName);
        return ResponseEntity.ok().build();
    }
}