package com.nephroforum.repository;

import com.nephroforum.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByOwnerNameOrderBySavedAtDesc(String ownerName);

    Optional<Favorite> findByOwnerNameAndPostId(String ownerName, Long postId);

    boolean existsByOwnerNameAndPostId(String ownerName, Long postId);
}