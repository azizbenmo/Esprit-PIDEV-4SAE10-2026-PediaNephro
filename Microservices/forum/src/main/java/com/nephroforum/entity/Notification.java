package com.nephroforum.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    private Long referenceId;

    @Column(name = "is_read")
    @Builder.Default
    private boolean read = false;

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum NotificationType {
        NEW_COMMENT,
        NEW_REACTION,
        NEW_MESSAGE,
        NEW_POST,
        FOLLOW_REQUEST,
        FOLLOW_RESPONSE
    }
}