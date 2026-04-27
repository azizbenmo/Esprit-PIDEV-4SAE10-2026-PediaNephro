package com.nephroforum.controller;

import com.nephroforum.dto.NotificationDTOs;
import com.nephroforum.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{recipientName}")
    public ResponseEntity<List<NotificationDTOs.NotificationResponse>> getAll(
            @PathVariable String recipientName) {
        return ResponseEntity.ok(notificationService.getAll(recipientName));
    }

    @GetMapping("/{recipientName}/unread-count")
    public ResponseEntity<Long> countUnread(
            @PathVariable String recipientName) {
        return ResponseEntity.ok(notificationService.countUnread(recipientName));
    }

    @PutMapping("/{recipientName}/mark-read")
    public ResponseEntity<Void> markRead(
            @PathVariable String recipientName) {
        notificationService.markAllRead(recipientName);
        return ResponseEntity.noContent().build();
    }
}
