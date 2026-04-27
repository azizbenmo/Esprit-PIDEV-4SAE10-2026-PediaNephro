package com.nephroforum.service;

import com.nephroforum.entity.*;
import com.nephroforum.entity.Reaction.ReactionType;
import com.nephroforum.exception.ResourceNotFoundException;
import com.nephroforum.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepo;
    private final PostRepository postRepo;
    private final CommentRepository commentRepo;
    private final NotificationService notificationService;

    @Transactional
    public boolean toggle(Long postId, Long commentId, ReactionType type) {

        if (postId != null) {
            Post post = postRepo.findById(postId)
                    .orElseThrow(() -> new ResourceNotFoundException("Post not found."));

            List<Reaction> existing = reactionRepo.findByPostId(postId);
            if (!existing.isEmpty()) {
                Reaction current = existing.get(0);
                if (current.getReactionType() == type) {
                    reactionRepo.delete(current);
                    return false;
                }
                reactionRepo.delete(current);
            }

            reactionRepo.save(Reaction.builder().post(post).reactionType(type).build());

            // Notifier l'auteur du post
            if (post.getAuthorName() != null) {
                notificationService.send(
                        post.getAuthorName(),
                        "👍 Quelqu'un a réagi à votre post : " + post.getTitle(),
                        Notification.NotificationType.NEW_REACTION,
                        postId
                );
            }

            return true;
        }

        if (commentId != null) {
            Comment comment = commentRepo.findById(commentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Comment not found."));

            List<Reaction> existing = reactionRepo.findByCommentId(commentId);
            if (!existing.isEmpty()) {
                Reaction current = existing.get(0);
                if (current.getReactionType() == type) {
                    reactionRepo.delete(current);
                    return false;
                }
                reactionRepo.delete(current);
            }

            reactionRepo.save(Reaction.builder().comment(comment).reactionType(type).build());

            // Notifier le médecin si quelqu'un réagit à son commentaire
            String commentAuthor = comment.getAuthorName();
            if (commentAuthor.startsWith("Dr.")) {
                notificationService.send(
                        commentAuthor,
                        "👍 Quelqu'un a réagi à votre commentaire",
                        Notification.NotificationType.NEW_REACTION,
                        comment.getPost().getId()
                );
            }

            return true;
        }

        throw new IllegalArgumentException("postId or commentId must be provided.");
    }
}