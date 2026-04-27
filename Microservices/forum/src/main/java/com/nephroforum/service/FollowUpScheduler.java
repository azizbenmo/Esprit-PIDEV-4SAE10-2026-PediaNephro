package com.nephroforum.service;

import com.nephroforum.entity.Notification;
import com.nephroforum.entity.Post;
import com.nephroforum.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowUpScheduler {

    private final PostRepository postRepo;
    private final NotificationService notificationService;

    // S'exécute toutes les heures
    @Scheduled(fixedRate = 3600000)
    public void checkUnansweredPosts() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);

        List<Post> unanswered = postRepo.findUnansweredPostsOlderThan(threshold);

        if (unanswered.isEmpty()) return;

        System.out.println("🔔 " + unanswered.size() +
                " post(s) sans réponse depuis 24h");

        for (Post post : unanswered) {
            // Notifier le médecin disponible
            notificationService.send(
                    "Dr.Amina",
                    "🔔 Le post \"" + post.getTitle() + "\" est sans réponse " +
                            "depuis plus de 24h. Le patient attend votre aide.",
                    Notification.NotificationType.NEW_POST,
                    post.getId()
            );

            System.out.println("📨 Notification envoyée pour le post: "
                    + post.getTitle());
        }
    }
}