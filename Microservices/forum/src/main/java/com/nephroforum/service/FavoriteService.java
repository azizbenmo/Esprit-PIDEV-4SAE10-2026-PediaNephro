package com.nephroforum.service;

import com.nephroforum.dto.PostDTOs;
import com.nephroforum.entity.Favorite;
import com.nephroforum.exception.ResourceNotFoundException;
import com.nephroforum.repository.FavoriteRepository;
import com.nephroforum.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepo;
    private final PostRepository postRepo;
    private final PostService postService;

    @Transactional
    public boolean toggle(String ownerName, Long postId) {
        // Si déjà sauvegardé → supprimer
        if (favoriteRepo.existsByOwnerNameAndPostId(ownerName, postId)) {
            favoriteRepo.findByOwnerNameAndPostId(ownerName, postId)
                    .ifPresent(favoriteRepo::delete);
            return false;
        }

        // Sinon → sauvegarder
        var post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        favoriteRepo.save(Favorite.builder()
                .ownerName(ownerName)
                .post(post)
                .build());
        return true;
    }

    public List<PostDTOs.PostResponse> getFavorites(String ownerName) {
        return favoriteRepo.findByOwnerNameOrderBySavedAtDesc(ownerName)
                .stream()
                .map(f -> postService.toResponse(f.getPost()))
                .collect(Collectors.toList());
    }

    public boolean isFavorite(String ownerName, Long postId) {
        return favoriteRepo.existsByOwnerNameAndPostId(ownerName, postId);
    }
}