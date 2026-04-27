package com.nephroforum.service;

import com.nephroforum.dto.BadgeDTOs;
import com.nephroforum.entity.Badge;
import com.nephroforum.entity.Badge.BadgeType;
import com.nephroforum.repository.BadgeRepository;
import com.nephroforum.repository.CommentRepository;
import com.nephroforum.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepo;
    private final PostRepository postRepo;
    private final CommentRepository commentRepo;
    private final NotificationService notificationService;

    // ── Vérifier et attribuer les badges automatiquement ─────────────────────

    public void checkAndAwardBadges(String authorName) {
        long postCount = postRepo.countByAuthorName(authorName);
        long commentCount = commentRepo.countByAuthorName(authorName);

        // 🏆 Expert Rein — docteur avec 5+ commentaires
        if (commentCount >= 5) {
            awardBadge(authorName, BadgeType.EXPERT_REIN);
        }

        // ⭐ Guerrier Courageux — patient avec 3+ posts
        if (postCount >= 3) {
            awardBadge(authorName, BadgeType.GUERRIER_COURAGEUX);
        }
    }

    // 🎯 Meilleure Réponse — attribuée manuellement
    public void awardBestAnswer(Long commentId, String awardedBy) {
        commentRepo.findById(commentId).ifPresent(comment -> {
            awardBadge(comment.getAuthorName(), BadgeType.MEILLEURE_REPONSE);
        });
    }

    private void awardBadge(String ownerName, BadgeType type) {
        // Ne pas donner le même badge deux fois
        if (badgeRepo.existsByOwnerNameAndType(ownerName, type)) return;

        Badge badge = Badge.builder()
                .ownerName(ownerName)
                .type(type)
                .build();
        badgeRepo.save(badge);

        // Notifier l'utilisateur
        notificationService.send(
                ownerName,
                "🎉 Vous avez gagné le badge : " + getEmoji(type) + " " + getLabel(type),
                com.nephroforum.entity.Notification.NotificationType.NEW_POST,
                null
        );
    }

    public List<BadgeDTOs.BadgeResponse> getBadges(String ownerName) {
        return badgeRepo.findByOwnerName(ownerName)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private BadgeDTOs.BadgeResponse toResponse(Badge badge) {
        return BadgeDTOs.BadgeResponse.builder()
                .id(badge.getId())
                .ownerName(badge.getOwnerName())
                .type(badge.getType())
                .label(getLabel(badge.getType()))
                .emoji(getEmoji(badge.getType()))
                .earnedAt(badge.getEarnedAt())
                .build();
    }

    private String getLabel(BadgeType type) {
        return switch (type) {
            case EXPERT_REIN        -> "Expert Rein";
            case GUERRIER_COURAGEUX -> "Guerrier Courageux";
            case MEILLEURE_REPONSE  -> "Meilleure Réponse";
        };
    }

    private String getEmoji(BadgeType type) {
        return switch (type) {
            case EXPERT_REIN        -> "🏆";
            case GUERRIER_COURAGEUX -> "⭐";
            case MEILLEURE_REPONSE  -> "🎯";
        };
    }

    public void awardBadgeManually(String ownerName, String type, String awardedBy) {
        BadgeType badgeType = BadgeType.valueOf(type);
        awardBadge(ownerName, badgeType);
    }

    public List<BadgeDTOs.BadgeResponse> getAllBadges() {
        return badgeRepo.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

}