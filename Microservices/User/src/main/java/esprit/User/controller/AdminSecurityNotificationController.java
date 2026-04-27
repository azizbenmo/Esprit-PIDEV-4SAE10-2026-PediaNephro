package esprit.User.controller;

import esprit.User.dto.AdminSecurityNotificationDTO;
import esprit.User.services.AdminSecurityNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mic1/admin/security-notifications")
public class AdminSecurityNotificationController {

    private final AdminSecurityNotificationService notificationService;

    public AdminSecurityNotificationController(AdminSecurityNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminSecurityNotificationDTO>> list(
            @RequestParam(name = "unreadOnly", defaultValue = "false") boolean unreadOnly) {
        return ResponseEntity.ok(notificationService.listRecent(unreadOnly));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread()));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markSeen(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Integer>> markAllRead() {
        int updated = notificationService.markAllSeen();
        return ResponseEntity.ok(Map.of("updated", updated));
    }
}
