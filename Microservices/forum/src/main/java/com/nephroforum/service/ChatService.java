package com.nephroforum.service;

import com.nephroforum.dto.ChatDTOs;
import com.nephroforum.entity.ChatMessage;
import com.nephroforum.entity.ConversationSettings;
import com.nephroforum.repository.ChatMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nephroforum.repository.ConversationSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatRepo;
    private final ObjectMapper objectMapper;
    private final ConversationSettingsRepository settingsRepo;

    public ChatDTOs.ChatMessageResponse save(ChatDTOs.ChatMessageRequest req) {
        ChatMessage message = ChatMessage.builder()
                .senderName(req.getSenderName())
                .receiverName(req.getReceiverName())
                .content(req.getContent())
                .type(req.getType())
                .imageUrl(req.getImageUrl())
                .audioUrl(req.getAudioUrl())
                .pdfUrl(req.getPdfUrl())
                .replyToId(req.getReplyToId())
                .replyToContent(req.getReplyToContent())
                .replyToSender(req.getReplyToSender())
                .build();
        return toResponse(chatRepo.save(message));
    }

    public List<ChatDTOs.ChatMessageResponse> getConversation(String user1, String user2) {
        return chatRepo.findConversation(user1, user2)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void markSeen(String receiver, String sender) {
        chatRepo.markAllSeen(receiver, sender);
    }

    public long getUnreadCount(String receiver, String sender) {
        return chatRepo.countUnread(receiver, sender);
    }

    @Transactional
    public ChatDTOs.ChatMessageResponse addReaction(Long messageId, String user, String emoji) {
        ChatMessage msg = chatRepo.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        try {
            Map<String, List<String>> reactions = msg.getReactions() != null
                    ? objectMapper.readValue(msg.getReactions(),
                    objectMapper.getTypeFactory().constructMapType(
                            Map.class, String.class, List.class))
                    : new HashMap<>();

            // Toggle reaction
            reactions.computeIfAbsent(emoji, k -> new ArrayList<>());
            List<String> users = reactions.get(emoji);
            if (users.contains(user)) users.remove(user);
            else users.add(user);

            if (users.isEmpty()) reactions.remove(emoji);

            msg.setReactions(objectMapper.writeValueAsString(reactions));
            return toResponse(chatRepo.save(msg));
        } catch (Exception e) {
            return toResponse(msg);
        }
    }

    // Épingler/désépingler
    @Transactional
    public ChatDTOs.ChatMessageResponse togglePin(Long messageId) {
        ChatMessage msg = chatRepo.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        msg.setPinned(!msg.isPinned());
        return toResponse(chatRepo.save(msg));
    }

    // Modifier message
    @Transactional
    public ChatDTOs.ChatMessageResponse editMessage(Long messageId, String content) {
        ChatMessage msg = chatRepo.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        msg.setContent(content);
        msg.setEdited(true);
        return toResponse(chatRepo.save(msg));
    }

    // Supprimer message (soft delete)
    @Transactional
    public ChatDTOs.ChatMessageResponse deleteMessage(Long messageId) {
        ChatMessage msg = chatRepo.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        msg.setDeleted(true);
        msg.setContent("Message supprimé");
        return toResponse(chatRepo.save(msg));
    }

    // Transférer message
    @Transactional
    public ChatDTOs.ChatMessageResponse transferMessage(Long messageId, String targetUser) {
        ChatMessage original = chatRepo.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        ChatMessage transferred = ChatMessage.builder()
                .senderName(original.getSenderName())
                .receiverName(targetUser)
                .content(original.getContent())
                .type(original.getType())
                .imageUrl(original.getImageUrl())
                .pdfUrl(original.getPdfUrl())
                .transferredTo(targetUser)
                .build();

        return toResponse(chatRepo.save(transferred));
    }

    // Messages épinglés
    public List<ChatDTOs.ChatMessageResponse> getPinnedMessages(String user1, String user2) {
        return chatRepo.findConversation(user1, user2).stream()
                .filter(ChatMessage::isPinned)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Fichiers médias de la conversation
    public List<ChatDTOs.ChatMessageResponse> getMediaFiles(String user1, String user2) {
        return chatRepo.findConversation(user1, user2).stream()
                .filter(m -> m.getImageUrl() != null || m.getPdfUrl() != null || m.getAudioUrl() != null)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Settings conversation
    private String buildKey(String u1, String u2) {
        return u1.compareTo(u2) < 0 ? u1 + "_" + u2 : u2 + "_" + u1;
    }

    public ChatDTOs.ConversationSettingsDTO getSettings(String user1, String user2) {
        String key = buildKey(user1, user2);
        return settingsRepo.findByConversationKey(key)
                .map(s -> ChatDTOs.ConversationSettingsDTO.builder()
                        .customName(s.getCustomName())
                        .theme(s.getTheme())
                        .emoji(s.getEmoji())
                        .nickname1(s.getNickname1())
                        .nickname2(s.getNickname2())
                        .photoUrl(s.getPhotoUrl())
                        .build())
                .orElse(ChatDTOs.ConversationSettingsDTO.builder()
                        .theme("blue").emoji("👋").build());
    }

    @Transactional
    public ChatDTOs.ConversationSettingsDTO updateSettings(
            String user1, String user2,
            ChatDTOs.ConversationSettingsDTO dto) {
        String key = buildKey(user1, user2);
        ConversationSettings settings = settingsRepo.findByConversationKey(key)
                .orElse(ConversationSettings.builder().conversationKey(key).build());

        if (dto.getCustomName() != null) settings.setCustomName(dto.getCustomName());
        if (dto.getTheme() != null) settings.setTheme(dto.getTheme());
        if (dto.getEmoji() != null) settings.setEmoji(dto.getEmoji());
        if (dto.getNickname1() != null) settings.setNickname1(dto.getNickname1());
        if (dto.getNickname2() != null) settings.setNickname2(dto.getNickname2());
        if (dto.getPhotoUrl() != null) settings.setPhotoUrl(dto.getPhotoUrl());

        settingsRepo.save(settings);
        return dto;
    }

    // Mettre à jour toResponse pour inclure les nouveaux champs
    private ChatDTOs.ChatMessageResponse toResponse(ChatMessage msg) {
        return ChatDTOs.ChatMessageResponse.builder()
                .id(msg.getId())
                .senderName(msg.getSenderName())
                .receiverName(msg.getReceiverName())
                .content(msg.getContent())
                .type(msg.getType())
                .sentAt(msg.getSentAt())
                .seen(msg.isSeen())
                .imageUrl(msg.getImageUrl())
                .audioUrl(msg.getAudioUrl())
                .pdfUrl(msg.getPdfUrl())
                .reactions(msg.getReactions())
                .replyToId(msg.getReplyToId())
                .replyToContent(msg.getReplyToContent())
                .replyToSender(msg.getReplyToSender())
                .pinned(msg.isPinned())
                .edited(msg.isEdited())
                .deleted(msg.isDeleted())
                .transferredTo(msg.getTransferredTo())
                .build();
    }
}