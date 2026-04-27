package com.nephroforum.dto;

import com.nephroforum.entity.Notification.NotificationType;
import lombok.*;
import java.time.LocalDateTime;

public class NotificationDTOs {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class NotificationResponse {
        private Long id;
        private String recipientName;
        private String message;
        private NotificationType type;
        private Long referenceId;
        private boolean read;
        private LocalDateTime createdAt;
    }
}