package com.nephroforum.service;

import com.nephroforum.dto.CommentDTOs;
import com.nephroforum.entity.*;
import com.nephroforum.exception.ContentModeratedException;
import com.nephroforum.exception.ResourceNotFoundException;
import com.nephroforum.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepo;
    private final PostRepository postRepo;
    private final ReactionRepository reactionRepo;
    private final ModerationService moderationService;
    private final NotificationService notificationService;
    private final BadgeService badgeService;

    @Transactional
    public CommentDTOs.CommentResponse addComment(Long postId, String content,
                                                  String authorName, Long parentId,
                                                  boolean anonymous) {
        if (!moderationService.isAllowed(content))
            throw new ContentModeratedException("Comment contains prohibited content.");

        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        Comment parent = null;
        if (parentId != null) {
            parent = commentRepo.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found."));

            // Notifier le médecin si quelqu'un répond à son commentaire
            String parentAuthor = parent.getAuthorName();
            if (parentAuthor.startsWith("Dr.") && !parentAuthor.equals(authorName)) {
                notificationService.send(
                        parentAuthor,
                        "↩️ " + (anonymous ? "Patient Anonyme" : authorName) + " a répondu à votre commentaire",
                        Notification.NotificationType.NEW_COMMENT,
                        postId
                );
            }
        }

        Comment comment = Comment.builder()
                .content(content)
                .authorName(authorName)
                .anonymous(anonymous)
                .post(post)
                .parent(parent)
                .build();

        Comment savedComment = commentRepo.save(comment);
        CommentDTOs.CommentResponse response = toResponse(savedComment);

        // Notifier l'auteur du post
        if (post.getAuthorName() != null && !post.getAuthorName().equals(authorName)) {
            notificationService.send(
                    post.getAuthorName(),
                    (anonymous ? "Patient Anonyme" : authorName) +
                            " a commenté votre post : " + post.getTitle(),
                    Notification.NotificationType.NEW_COMMENT,
                    postId
            );
        }

        // Détecter les mentions @
        handleMentions(content, postId, authorName, anonymous);

        // Vérifier et attribuer les badges
        badgeService.checkAndAwardBadges(authorName);

        return response;
    }

    public List<CommentDTOs.CommentResponse> getComments(Long postId) {
        return commentRepo.findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public CommentDTOs.CommentResponse updateComment(Long commentId, String content) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found."));
        if (!moderationService.isAllowed(content))
            throw new ContentModeratedException("Comment contains prohibited content.");
        comment.setContent(content);
        return toResponse(commentRepo.save(comment));
    }

    @Transactional
    public CommentDTOs.CommentResponse markAsOfficial(Long postId, Long commentId) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
        comment.setOfficialResponse(!comment.isOfficialResponse()); // toggle
        return toResponse(commentRepo.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found."));
        commentRepo.delete(comment);
    }

    // ── Détecter les mentions @nom ────────────────────────────────────────────
    private void handleMentions(String content, Long postId, String senderName, boolean anonymous) {
        Pattern pattern = Pattern.compile("@(\\S+)");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String mentionedName = matcher.group(1);
            // Ne pas notifier soi-même
            if (mentionedName.equals(senderName)) continue;

            notificationService.send(
                    mentionedName,
                    "🔔 " + (anonymous ? "Patient Anonyme" : senderName) +
                            " vous a mentionné dans un commentaire.",
                    Notification.NotificationType.NEW_COMMENT,
                    postId
            );
            System.out.println("📨 Mention détectée : @" + mentionedName);
        }
    }

    public CommentDTOs.CommentResponse toResponse(Comment comment) {
        Map<String, Long> reactionCounts = new HashMap<>();
        for (Reaction.ReactionType r : Reaction.ReactionType.values())
            reactionCounts.put(r.name(), 0L);
        for (Object[] entry : reactionRepo.countByCommentId(comment.getId()))
            reactionCounts.put(entry[0].toString(), (Long) entry[1]);

        String displayName = comment.isAnonymous()
                ? "Patient Anonyme" : comment.getAuthorName();

        List<CommentDTOs.CommentResponse> replies = comment.getReplies().stream()
                .map(this::toResponse).collect(Collectors.toList());

        return CommentDTOs.CommentResponse.builder()
                .id(comment.getId())
                .content(comment.isModerated() ? "[moderated]" : comment.getContent())
                .authorName(comment.getAuthorName())
                .displayName(displayName)
                .anonymous(comment.isAnonymous())
                .reactions(reactionCounts)
                .replies(replies)
                .moderated(comment.isModerated())
                .createdAt(comment.getCreatedAt())
                .officialResponse(comment.isOfficialResponse())
                .build();
    }


    public List<CommentDTOs.CommentResponse> getMyComments(String authorName) {
        return commentRepo.findByAuthorName(authorName)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

}