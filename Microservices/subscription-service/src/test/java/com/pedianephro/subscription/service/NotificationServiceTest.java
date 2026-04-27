package com.pedianephro.subscription.service;

import com.pedianephro.subscription.dto.NotificationDto;
import com.pedianephro.subscription.entity.Notification;
import com.pedianephro.subscription.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createNotification_shouldPersistUnreadNotification() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        NotificationDto dto = notificationService.createNotification(1L, "Titre", "Message", "INFO");

        assertNotNull(dto);
        assertEquals(1L, dto.getUserId());
        assertEquals("Titre", dto.getTitle());
        assertEquals("Message", dto.getMessage());
        assertEquals("INFO", dto.getType());
        assertFalse(dto.isReadStatus());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertFalse(saved.isReadStatus());
    }

    @Test
    void getNotifications_shouldReturnDtos() {
        Notification n1 = new Notification(1L, 1L, "T1", "M1", "INFO", false, LocalDateTime.now());
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(n1));

        List<NotificationDto> list = notificationService.getNotifications(1L);

        assertEquals(1, list.size());
        assertEquals("T1", list.get(0).getTitle());
    }

    @Test
    void getUnreadCount_shouldDelegateToRepository() {
        when(notificationRepository.countByUserIdAndReadStatusFalse(2L)).thenReturn(5L);
        assertEquals(5L, notificationService.getUnreadCount(2L));
    }

    @Test
    void markAsRead_shouldThrowNotFound_whenMissing() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> notificationService.markAsRead(10L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void markAsRead_shouldSetReadStatusTrue_andSave() {
        Notification n = new Notification(10L, 1L, "T", "M", "INFO", false, LocalDateTime.now());
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));

        notificationService.markAsRead(10L);

        assertTrue(n.isReadStatus());
        verify(notificationRepository).save(eq(n));
    }

    @Test
    void markAllAsRead_shouldPersist_whenUnreadExist() {
        Notification n = new Notification(1L, 3L, "T", "M", "INFO", false, LocalDateTime.now());
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(3L)).thenReturn(List.of(n));
        when(notificationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.markAllAsRead(3L);

        assertTrue(n.isReadStatus());
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    void markAllAsRead_shouldSkipSave_whenAlreadyAllRead() {
        Notification n = new Notification(1L, 3L, "T", "M", "INFO", true, LocalDateTime.now());
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(3L)).thenReturn(List.of(n));

        notificationService.markAllAsRead(3L);

        verify(notificationRepository, never()).saveAll(anyList());
    }

    @Test
    void markExpirationReminderAsRead_shouldCallRepositoryWithTitle() {
        notificationService.markExpirationReminderAsRead(3L);
        verify(notificationRepository).markUnreadByTitleAsRead(eq(3L), eq("Abonnement bientôt expiré"));
    }
}
