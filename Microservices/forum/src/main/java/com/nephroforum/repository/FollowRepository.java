package com.nephroforum.repository;

import com.nephroforum.entity.Follow;
import com.nephroforum.entity.Follow.FollowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerNameAndFollowingName(
            String followerName, String followingName);

    List<Follow> findByFollowingNameAndStatus(
            String followingName, FollowStatus status);

    List<Follow> findByFollowerNameAndStatus(
            String followerName, FollowStatus status);

    boolean existsByFollowerNameAndFollowingNameAndStatus(
            String followerName, String followingName, FollowStatus status);

    long countByFollowingNameAndStatus(String followingName, FollowStatus status);
    long countByFollowerNameAndStatus(String followerName, FollowStatus status);

    // Tous les followers acceptés d'un utilisateur
    @Query("SELECT f.followerName FROM Follow f WHERE f.followingName = :name AND f.status = 'ACCEPTED'")
    List<String> findAcceptedFollowerNames(@Param("name") String name);

    void deleteByFollowerNameAndFollowingName(String followerName, String followingName);
}