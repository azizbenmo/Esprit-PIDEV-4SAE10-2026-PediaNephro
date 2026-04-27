package esprit.User.services;

import esprit.User.dto.AdminSecurityNotificationDTO;
import esprit.User.entities.AdminSecurityNotification;
import esprit.User.repositories.AdminSecurityNotificationRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminSecurityNotificationService {

    public static final String TYPE_SQL_INJECTION_LOGIN = "SQL_INJECTION_LOGIN";

    private final AdminSecurityNotificationRepository repository;

    public AdminSecurityNotificationService(AdminSecurityNotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void recordSqlInjectionLoginAttempt(HttpServletRequest request, String usernameOrEmail) {
        String ip = clientIp(request);
        String ua = request.getHeader("User-Agent");
        String uaSnippet = ua == null ? null : ua.length() > 400 ? ua.substring(0, 400) : ua;
        String hint = sanitizeUsernameHint(usernameOrEmail);

        AdminSecurityNotification n = AdminSecurityNotification.builder()
                .type(TYPE_SQL_INJECTION_LOGIN)
                .message("Tentative d'injection SQL détectée sur le formulaire de connexion.")
                .clientIp(ip != null ? ip : "inconnu")
                .userAgentSnippet(uaSnippet)
                .usernameHint(hint)
                .seenByAdmin(false)
                .build();
        repository.save(n);
    }

    private static String sanitizeUsernameHint(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            return null;
        }
        String t = usernameOrEmail.trim().replaceAll("[\r\n\t]", " ");
        return t.length() > 120 ? t.substring(0, 120) + "…" : t;
    }

    private static String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Transactional(readOnly = true)
    public List<AdminSecurityNotificationDTO> listRecent(boolean unreadOnly) {
        List<AdminSecurityNotification> rows = unreadOnly
                ? repository.findTop50BySeenByAdminFalseOrderByCreatedAtDesc()
                : repository.findTop50ByOrderByCreatedAtDesc();
        return rows.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        return repository.countBySeenByAdminFalse();
    }

    @Transactional
    public void markSeen(Long id) {
        repository.findById(id).ifPresent(n -> {
            n.setSeenByAdmin(true);
            repository.save(n);
        });
    }

    @Transactional
    public int markAllSeen() {
        return repository.markAllUnreadAsSeen();
    }

    private AdminSecurityNotificationDTO toDto(AdminSecurityNotification n) {
        return AdminSecurityNotificationDTO.builder()
                .id(n.getId())
                .type(n.getType())
                .message(n.getMessage())
                .clientIp(n.getClientIp())
                .userAgentSnippet(n.getUserAgentSnippet())
                .usernameHint(n.getUsernameHint())
                .seenByAdmin(n.isSeenByAdmin())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
