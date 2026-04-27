package com.nephroforum.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String senderName;

    @Column(nullable = false)
    private String receiverName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    private boolean seen = false;

    // ← NOUVEAUX CHAMPS
    private boolean pinned = false;
    private boolean edited = false;
    private String transferredTo;
    private boolean deleted = false;

    public enum MessageType { CHAT, JOIN, LEAVE }

    private String imageUrl;
    private String audioUrl;
    private String pdfUrl;

    @Column(columnDefinition = "TEXT")
    private String reactions;
    private Long replyToId;
    private String replyToContent;
    private String replyToSender;
}