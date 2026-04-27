package com.nephroforum.controller;

import com.nephroforum.dto.PostDTOs;
import com.nephroforum.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // Ajouter ou retirer des favoris (toggle)
    @PostMapping("/{ownerName}/{postId}")
    public ResponseEntity<Boolean> toggle(
            @PathVariable String ownerName,
            @PathVariable Long postId) {
        return ResponseEntity.ok(favoriteService.toggle(ownerName, postId));
    }

    // Voir tous les favoris d'un utilisateur
    @GetMapping("/{ownerName}")
    public ResponseEntity<List<PostDTOs.PostResponse>> getFavorites(
            @PathVariable String ownerName) {
        return ResponseEntity.ok(favoriteService.getFavorites(ownerName));
    }

    // Vérifier si un post est en favori
    @GetMapping("/{ownerName}/{postId}/check")
    public ResponseEntity<Boolean> check(
            @PathVariable String ownerName,
            @PathVariable Long postId) {
        return ResponseEntity.ok(favoriteService.isFavorite(ownerName, postId));
    }
}
