package com.pedianephro.subscription.service;

import com.pedianephro.subscription.dto.NotificationDto;
import com.pedianephro.subscription.entity.Notification;
import com.pedianephro.subscription.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private static final String EXPIRATION_TITLE = "Abonnement bientôt expiré";

    public NotificationDto createNotification(Long userId, String title, String message, String type) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setReadStatus(false);
        return toDto(notificationRepository.save(n));
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadStatusFalse(userId);
    }

    public void markAsRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification introuvable"));
        n.setReadStatus(true);
        notificationRepository.save(n);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        boolean changed = false;
        for (Notification n : notifications) {
            if (!n.isReadStatus()) {
                n.setReadStatus(true);
                changed = true;
            }
        }
        if (changed) {
            notificationRepository.saveAll(notifications);
        }
    }

    public void markExpirationReminderAsRead(Long userId) {
        notificationRepository.markUnreadByTitleAsRead(userId, EXPIRATION_TITLE);
    }

    private NotificationDto toDto(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getUserId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.isReadStatus(),
                n.getCreatedAt()
        );
    }
}
