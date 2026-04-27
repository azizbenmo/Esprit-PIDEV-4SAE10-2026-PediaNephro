package com.nephroforum.controller;

import com.nephroforum.dto.ChatDTOs;
import com.nephroforum.entity.ChatMessage;
import com.nephroforum.entity.Notification;
import com.nephroforum.service.AIService;
import com.nephroforum.service.ChatService;
import com.nephroforum.service.ModerationService;
import com.nephroforum.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChatController {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ModerationService moderationService;
    private final AIService aiService;
    private final NotificationService notificationService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatDTOs.ChatMessageRequest req) {

        if (!moderationService.isAllowed(req.getContent())) {
            ChatDTOs.ChatMessageResponse moderated = ChatDTOs.ChatMessageResponse.builder()
                    .senderName("System")
                    .receiverName(req.getSenderName())
                    .content("⚠️ Votre message contient du contenu non autorisé.")
                    .type(ChatMessage.MessageType.CHAT)
                    .sentAt(java.time.LocalDateTime.now())
                    .build();
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + req.getSenderName(), moderated);
            return;
        }

        ChatDTOs.ChatMessageResponse saved = chatService.save(req);
        messagingTemplate.convertAndSend("/topic/chat/" + req.getReceiverName(), saved);
        messagingTemplate.convertAndSend("/topic/chat/" + req.getSenderName(), saved);

        notificationService.send(
                req.getReceiverName(),
                req.getSenderName() + " vous a envoyé un message",
                Notification.NotificationType.NEW_MESSAGE,
                null
        );
    }

    @PostMapping("/api/chat/translate")
    public ResponseEntity<String> translateMessage(
            @RequestParam String text,
            @RequestParam String targetLang) {
        return ResponseEntity.ok(aiService.translate(text, targetLang));
    }

    @PostMapping(value = "/api/chat/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(
            @RequestParam MultipartFile image) throws IOException {
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
        Files.copy(image.getInputStream(), dir.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING);
        return ResponseEntity.ok("/uploads/" + filename);
    }

    @PostMapping(value = "/api/chat/upload-audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAudio(
            @RequestParam MultipartFile audio) throws IOException {
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        String filename = UUID.randomUUID() + "_" + audio.getOriginalFilename();
        Files.copy(audio.getInputStream(), dir.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING);
        return ResponseEntity.ok("/uploads/" + filename);
    }

    @PostMapping(value = "/api/chat/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadPdf(
            @RequestParam MultipartFile pdf) throws IOException {

        // Vérifier que c'est bien un PDF
        String originalFilename = pdf.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf"))
            return ResponseEntity.badRequest().body("Le fichier doit être un PDF.");

        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        String filename = UUID.randomUUID() + "_" + originalFilename;
        Files.copy(pdf.getInputStream(), dir.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING);
        return ResponseEntity.ok("/uploads/" + filename);
    }

    @GetMapping("/api/chat/{user1}/{user2}")
    public ResponseEntity<List<ChatDTOs.ChatMessageResponse>> getConversation(
            @PathVariable String user1,
            @PathVariable String user2) {
        return ResponseEntity.ok(chatService.getConversation(user1, user2));
    }

    // Marquer messages comme vus
    @PostMapping("/api/chat/seen")
    public ResponseEntity<Void> markSeen(
            @RequestParam String receiver,
            @RequestParam String sender) {
        chatService.markSeen(receiver, sender);
        return ResponseEntity.ok().build();
    }

    // Compter non-lus
    @GetMapping("/api/chat/unread")
    public ResponseEntity<Long> getUnread(
            @RequestParam String receiver,
            @RequestParam String sender) {
        return ResponseEntity.ok(chatService.getUnreadCount(receiver, sender));
    }

    // Réaction sur message
    @PostMapping("/api/chat/react")
    public ResponseEntity<ChatDTOs.ChatMessageResponse> react(
            @RequestParam Long messageId,
            @RequestParam String user,
            @RequestParam String emoji) {
        ChatDTOs.ChatMessageResponse updated = chatService.addReaction(messageId, user, emoji);
        // Broadcast la réaction
        messagingTemplate.convertAndSend("/topic/chat/" + updated.getReceiverName(), updated);
        messagingTemplate.convertAndSend("/topic/chat/" + updated.getSenderName(), updated);
        return ResponseEntity.ok(updated);
    }

    // Typing indicator via WebSocket
    @MessageMapping("/chat.typing")
    public void typing(@Payload ChatDTOs.TypingRequest req) {
        messagingTemplate.convertAndSend(
                "/topic/typing/" + req.getReceiverName(), req);
    }

    // Épingler
    @PostMapping("/api/chat/pin/{messageId}")
    public ResponseEntity<ChatDTOs.ChatMessageResponse> togglePin(
            @PathVariable Long messageId) {
        ChatDTOs.ChatMessageResponse updated = chatService.togglePin(messageId);
        messagingTemplate.convertAndSend("/topic/chat/" + updated.getReceiverName(), updated);
        messagingTemplate.convertAndSend("/topic/chat/" + updated.getSenderName(), updated);
        return ResponseEntity.ok(updated);
    }

    // Modifier
    @PutMapping("/api/chat/message/{messageId}")
    public ResponseEntity<ChatDTOs.ChatMessageResponse> editMessage(
            @PathVariable Long messageId,
            @RequestBody ChatDTOs.EditMessageRequest req) {
        ChatDTOs.ChatMessageResponse updated = chatService.editMessage(messageId, req.getContent());
        messagingTemplate.convertAndSend("/topic/chat/" + updated.getReceiverName(), updated);
        messagingTemplate.convertAndSend("/topic/chat/" + updated.getSenderName(), updated);
        return ResponseEntity.ok(updated);
    }

    // Supprimer
    @DeleteMapping("/api/chat/message/{messageId}")
    public ResponseEntity<ChatDTOs.ChatMessageResponse> deleteMessage(
            @PathVariable Long messageId) {
        ChatDTOs.ChatMessageResponse updated = chatService.deleteMessage(messageId);
        messagingTemplate.convertAndSend("/topic/chat/" + updated.getReceiverName(), updated);
        messagingTemplate.convertAndSend("/topic/chat/" + updated.getSenderName(), updated);
        return ResponseEntity.ok(updated);
    }

    // Transférer
    @PostMapping("/api/chat/transfer/{messageId}")
    public ResponseEntity<ChatDTOs.ChatMessageResponse> transferMessage(
            @PathVariable Long messageId,
            @RequestBody ChatDTOs.TransferRequest req) {
        return ResponseEntity.ok(chatService.transferMessage(messageId, req.getTargetUser()));
    }

    // Messages épinglés
    @GetMapping("/api/chat/pinned/{user1}/{user2}")
    public ResponseEntity<List<ChatDTOs.ChatMessageResponse>> getPinned(
            @PathVariable String user1, @PathVariable String user2) {
        return ResponseEntity.ok(chatService.getPinnedMessages(user1, user2));
    }

    // Fichiers médias
    @GetMapping("/api/chat/media/{user1}/{user2}")
    public ResponseEntity<List<ChatDTOs.ChatMessageResponse>> getMedia(
            @PathVariable String user1, @PathVariable String user2) {
        return ResponseEntity.ok(chatService.getMediaFiles(user1, user2));
    }

    // Settings
    @GetMapping("/api/chat/settings/{user1}/{user2}")
    public ResponseEntity<ChatDTOs.ConversationSettingsDTO> getSettings(
            @PathVariable String user1, @PathVariable String user2) {
        return ResponseEntity.ok(chatService.getSettings(user1, user2));
    }

    @PutMapping("/api/chat/settings/{user1}/{user2}")
    public ResponseEntity<ChatDTOs.ConversationSettingsDTO> updateSettings(
            @PathVariable String user1, @PathVariable String user2,
            @RequestBody ChatDTOs.ConversationSettingsDTO dto) {
        return ResponseEntity.ok(chatService.updateSettings(user1, user2, dto));
    }

    // GIF upload
    @PostMapping(value = "/api/chat/upload-gif", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadGif(
            @RequestParam MultipartFile gif) throws IOException {
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        String filename = UUID.randomUUID() + "_" + gif.getOriginalFilename();
        Files.copy(gif.getInputStream(), dir.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING);
        return ResponseEntity.ok("/uploads/" + filename);
    }

}