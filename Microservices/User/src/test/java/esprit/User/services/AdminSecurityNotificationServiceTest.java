package esprit.User.services;

import esprit.User.dto.AdminSecurityNotificationDTO;
import esprit.User.entities.AdminSecurityNotification;
import esprit.User.repositories.AdminSecurityNotificationRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSecurityNotificationServiceTest {

    @Mock
    private AdminSecurityNotificationRepository repository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AdminSecurityNotificationService service;

    @Test
    void recordSqlInjectionLoginAttempt_shouldSaveNotificationWithSanitizedData() {
        String veryLongUserAgent = "Mozilla/5.0 ".repeat(50);
        String usernameWithControlChars = "  bad.user@example.com\r\n\tOR 1=1  ";

        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.7, 10.0.0.12");
        when(request.getHeader("User-Agent")).thenReturn(veryLongUserAgent);

        service.recordSqlInjectionLoginAttempt(request, usernameWithControlChars);

        ArgumentCaptor<AdminSecurityNotification> captor = ArgumentCaptor.forClass(AdminSecurityNotification.class);
        verify(repository).save(captor.capture());
        AdminSecurityNotification saved = captor.getValue();

        assertThat(saved.getType()).isEqualTo(AdminSecurityNotificationService.TYPE_SQL_INJECTION_LOGIN);
        assertThat(saved.getClientIp()).isEqualTo("203.0.113.7");
        assertThat(saved.isSeenByAdmin()).isFalse();
        assertThat(saved.getUserAgentSnippet()).hasSizeLessThanOrEqualTo(400);
        assertThat(saved.getUsernameHint()).doesNotContain("\r").doesNotContain("\n").doesNotContain("\t");
    }

    @Test
    void listRecent_shouldUseUnreadQueryWhenRequested() {
        AdminSecurityNotification unread = AdminSecurityNotification.builder()
                .id(1L)
                .type(AdminSecurityNotificationService.TYPE_SQL_INJECTION_LOGIN)
                .message("msg")
                .clientIp("198.51.100.8")
                .seenByAdmin(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findTop50BySeenByAdminFalseOrderByCreatedAtDesc()).thenReturn(List.of(unread));

        List<AdminSecurityNotificationDTO> result = service.listRecent(true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isSeenByAdmin()).isFalse();
        verify(repository).findTop50BySeenByAdminFalseOrderByCreatedAtDesc();
    }

    @Test
    void listRecent_shouldUseAllQueryWhenUnreadFilterDisabled() {
        AdminSecurityNotification read = AdminSecurityNotification.builder()
                .id(2L)
                .type(AdminSecurityNotificationService.TYPE_SQL_INJECTION_LOGIN)
                .message("msg")
                .clientIp("198.51.100.9")
                .seenByAdmin(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findTop50ByOrderByCreatedAtDesc()).thenReturn(List.of(read));

        List<AdminSecurityNotificationDTO> result = service.listRecent(false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isSeenByAdmin()).isTrue();
        verify(repository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    void markSeen_shouldUpdateAndSaveWhenNotificationExists() {
        AdminSecurityNotification row = AdminSecurityNotification.builder()
                .id(55L)
                .seenByAdmin(false)
                .type(AdminSecurityNotificationService.TYPE_SQL_INJECTION_LOGIN)
                .message("x")
                .clientIp("127.0.0.1")
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findById(55L)).thenReturn(Optional.of(row));

        service.markSeen(55L);

        assertThat(row.isSeenByAdmin()).isTrue();
        verify(repository).save(row);
    }

    @Test
    void markSeen_shouldDoNothingWhenNotificationDoesNotExist() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        service.markSeen(999L);

        verify(repository, never()).save(any(AdminSecurityNotification.class));
    }

    @Test
    void countUnread_and_markAllSeen_shouldDelegateToRepository() {
        when(repository.countBySeenByAdminFalse()).thenReturn(6L);
        when(repository.markAllUnreadAsSeen()).thenReturn(6);

        long unread = service.countUnread();
        int updated = service.markAllSeen();

        assertThat(unread).isEqualTo(6L);
        assertThat(updated).isEqualTo(6);
    }
}
