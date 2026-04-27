package com.nephroforum.controller;

import com.nephroforum.dto.ChatbotDTOs;
import com.nephroforum.service.ChatbotService;
import com.nephroforum.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final NotificationService notificationService;

    @PostMapping("/ask")
    public ResponseEntity<ChatbotDTOs.ChatbotResponse> ask(
            @RequestBody ChatbotDTOs.ChatbotRequest req) {

        ChatbotDTOs.ChatbotResponse response = chatbotService.answer(req);

        // Si cas critique → notifier tous les médecins
        if (response.isCriticalCase()) {
            notificationService.send(
                    "Dr.Amina",
                    "🚨 URGENT : " + req.getProfile().getPatientName() +
                            " a signalé un symptôme critique via le chatbot.",
                    com.nephroforum.entity.Notification.NotificationType.NEW_POST,
                    null
            );
        }

        return ResponseEntity.ok(response);
    }
}