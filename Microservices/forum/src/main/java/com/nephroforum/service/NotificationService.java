package com.nephroforum.service;

import com.nephroforum.dto.NotificationDTOs;
import com.nephroforum.entity.Notification;
import com.nephroforum.entity.Notification.NotificationType;
import com.nephroforum.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public void send(String recipientName, String message,
                     NotificationType type, Long referenceId) {

        // Sauvegarder en base
        Notification notification = Notification.builder()
                .recipientName(recipientName)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .build();

        NotificationDTOs.NotificationResponse response =
                toResponse(notificationRepo.save(notification));

        // Envoyer en temps réel via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + recipientName, response);
    }

    public List<NotificationDTOs.NotificationResponse> getAll(String recipientName) {
        return notificationRepo
                .findByRecipientNameOrderByCreatedAtDesc(recipientName)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public long countUnread(String recipientName) {
        return notificationRepo.countByRecipientNameAndReadFalse(recipientName);
    }

    public void markAllRead(String recipientName) {
        List<Notification> notifications = notificationRepo
                .findByRecipientNameOrderByCreatedAtDesc(recipientName);
        notifications.forEach(n -> n.setRead(true));
        notificationRepo.saveAll(notifications);
    }

    private NotificationDTOs.NotificationResponse toResponse(Notification n) {
        return NotificationDTOs.NotificationResponse.builder()
                .id(n.getId())
                .recipientName(n.getRecipientName())
                .message(n.getMessage())
                .type(n.getType())
                .referenceId(n.getReferenceId())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}