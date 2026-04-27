package com.nephroforum.dto;

import com.nephroforum.entity.ChatMessage.MessageType;
import lombok.*;
import java.time.LocalDateTime;

public class ChatDTOs {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ChatMessageRequest {
        private String senderName;
        private String receiverName;
        private String content;
        private MessageType type;
        private String imageUrl;
        private String audioUrl;
        private String pdfUrl;
        private Long replyToId;
        private String replyToContent;
        private String replyToSender;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ChatMessageResponse {
        private Long id;
        private String senderName;
        private String receiverName;
        private String content;
        private MessageType type;
        private LocalDateTime sentAt;
        private boolean seen;
        private String imageUrl;
        private String audioUrl;
        private String pdfUrl;
        private String reactions;
        private Long replyToId;
        private String replyToContent;
        private String replyToSender;
        private int unreadCount;
        // ← NOUVEAUX
        private boolean pinned;
        private boolean edited;
        private boolean deleted;
        private String transferredTo;
    }

    @Getter @Setter
    public static class TypingRequest {
        private String senderName;
        private String receiverName;
        private boolean typing;
    }

    // ← NOUVEAU : settings discussion
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ConversationSettingsDTO {
        private String customName;
        private String theme;
        private String emoji;
        private String nickname1;
        private String nickname2;
        private String photoUrl;
    }

    // ← NOUVEAU : modifier un message
    @Getter @Setter
    public static class EditMessageRequest {
        private String content;
    }

    // ← NOUVEAU : transfert
    @Getter @Setter
    public static class TransferRequest {
        private String targetUser;
    }
}