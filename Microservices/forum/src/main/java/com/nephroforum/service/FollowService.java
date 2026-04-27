package com.nephroforum.service;

import com.nephroforum.dto.FollowDTOs;
import com.nephroforum.entity.Follow;
import com.nephroforum.entity.Follow.FollowStatus;
import com.nephroforum.entity.Notification.NotificationType;
import com.nephroforum.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepo;
    private final NotificationService notificationService;

    // ── Envoyer une demande de suivi ─────────────────────────────────────────

    @Transactional
    public FollowDTOs.FollowResponse sendRequest(String followerName,
                                                 String followingName) {
        // Vérifie si une demande existe déjà
        followRepo.findByFollowerNameAndFollowingName(followerName, followingName)
                .ifPresent(f -> {
                    throw new IllegalStateException("Une demande existe déjà.");
                });

        Follow follow = followRepo.save(Follow.builder()
                .followerName(followerName)
                .followingName(followingName)
                .status(FollowStatus.PENDING)
                .build());

        // Notifie la cible
        notificationService.send(
                followingName,
                "👥 " + followerName + " souhaite vous suivre",
                NotificationType.FOLLOW_REQUEST,
                follow.getId()
        );

        return toResponse(follow);
    }

    // ── Accepter ou rejeter ──────────────────────────────────────────────────

    @Transactional
    public FollowDTOs.FollowResponse respond(Long followId,
                                             String responderName,
                                             boolean accept) {
        Follow follow = followRepo.findById(followId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        if (!follow.getFollowingName().equals(responderName))
            throw new IllegalStateException("Non autorisé");

        follow.setStatus(accept ? FollowStatus.ACCEPTED : FollowStatus.REJECTED);
        follow.setRespondedAt(LocalDateTime.now());
        followRepo.save(follow);

        // Notifie le demandeur
        String msg = accept
                ? "✅ " + responderName + " a accepté votre demande de suivi"
                : "❌ " + responderName + " a refusé votre demande de suivi";

        notificationService.send(
                follow.getFollowerName(), msg,
                NotificationType.FOLLOW_RESPONSE,
                follow.getId()
        );

        return toResponse(follow);
    }

    // ── Se désabonner ────────────────────────────────────────────────────────

    @Transactional
    public void unfollow(String followerName, String followingName) {
        followRepo.deleteByFollowerNameAndFollowingName(followerName, followingName);
    }

    // ── Demandes reçues en attente ───────────────────────────────────────────

    public List<FollowDTOs.FollowResponse> getPendingRequests(String username) {
        return followRepo.findByFollowingNameAndStatus(username, FollowStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Liste des followers acceptés ─────────────────────────────────────────

    public List<FollowDTOs.FollowResponse> getFollowers(String username) {
        return followRepo.findByFollowingNameAndStatus(username, FollowStatus.ACCEPTED)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Liste des following acceptés ─────────────────────────────────────────

    public List<FollowDTOs.FollowResponse> getFollowing(String username) {
        return followRepo.findByFollowerNameAndStatus(username, FollowStatus.ACCEPTED)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Statut entre deux utilisateurs ───────────────────────────────────────

    public String getStatus(String followerName, String followingName) {
        return followRepo.findByFollowerNameAndFollowingName(followerName, followingName)
                .map(f -> f.getStatus().name())
                .orElse("NONE");
    }

    // ── Compteurs ────────────────────────────────────────────────────────────

    public long countFollowers(String username) {
        return followRepo.countByFollowingNameAndStatus(username, FollowStatus.ACCEPTED);
    }

    public long countFollowing(String username) {
        return followRepo.countByFollowerNameAndStatus(username, FollowStatus.ACCEPTED);
    }

    // ── Notifier les followers lors d'un nouveau post ────────────────────────

    public void notifyFollowers(String authorName, Long postId, String postTitle) {
        List<String> followers = followRepo.findAcceptedFollowerNames(authorName);
        for (String follower : followers) {
            notificationService.send(
                    follower,
                    "📝 " + authorName + " a publié : " + postTitle,
                    NotificationType.NEW_POST,
                    postId
            );
        }
    }

    private FollowDTOs.FollowResponse toResponse(Follow f) {
        return FollowDTOs.FollowResponse.builder()
                .id(f.getId())
                .followerName(f.getFollowerName())
                .followingName(f.getFollowingName())
                .status(f.getStatus())
                .createdAt(f.getCreatedAt())
                .respondedAt(f.getRespondedAt())
                .build();
    }
    public void cancelRequest(String followerName, String followingName) {
        Follow follow = followRepo
                .findByFollowerNameAndFollowingName(followerName, followingName)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));
        if (follow.getStatus() != FollowStatus.PENDING) {
            throw new RuntimeException("Seules les demandes en attente peuvent être annulées");
        }
        followRepo.delete(follow);
    }
}