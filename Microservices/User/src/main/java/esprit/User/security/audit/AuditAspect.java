package esprit.User.security.audit;

import esprit.User.entities.AuditLog;
import esprit.User.repositories.AuditLogRepository;
import esprit.User.dto.ApiResponse;
import esprit.User.dto.LoginResponse;
import esprit.User.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // S'exécute APRÈS le succès de la méthode annotée
    @AfterReturning(pointcut = "@annotation(logAction)", returning = "result")
    public void logAfterMethodCall(JoinPoint joinPoint, LogAction logAction, Object result) {
        
        // Extraction du contexte HTTP actuel
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return;
        HttpServletRequest request = attributes.getRequest();

        // Extractions de données
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        // Extraction de l'action depuis l'annotation
        final String action = logAction.action();
        
        // Extraction de l'Identité de l'utilisateur
        String username = null;
        Long userId = null;
        
        // 1. Essayer via le SecurityContext (si déjà peuplé)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            username = auth.getName();
            if (auth.getPrincipal() instanceof CustomUserDetails) {
                userId = ((CustomUserDetails) auth.getPrincipal()).getId();
            }
        }

        // 2. Si non trouvé (ex: juste après un login réussi), essayer d'extraire depuis le résultat retourné
        // Ne journaliser que les actions qui ont réellement réussi.
        // Comme l'aspect s'exécute après retour, on considère "succès" = réponse HTTP 2xx
        // (et, si un ApiResponse est utilisé, success == true).
        if (result instanceof ResponseEntity<?> responseEntity) {
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                return;
            }
            Object body = responseEntity.getBody();
            if (body instanceof ApiResponse<?> apiResponse && !apiResponse.isSuccess()) {
                return;
            }
            if (body instanceof Map<?, ?> map) {
                Object success = map.get("success");
                if (success instanceof Boolean b && !b) {
                    return;
                }
            }
        }

        String details = null;
        if (result instanceof ResponseEntity<?> responseEntity) {
            Object body = responseEntity.getBody();
            if (body instanceof LoginResponse loginResponse) {
                if (userId == null) userId = loginResponse.getId();
                if (username == null) username = loginResponse.getUsername();
                details = loginResponse.getMessage();
            } else if (body instanceof ApiResponse<?> apiResponse) {
                details = apiResponse.getMessage();
            } else if (body instanceof Map<?, ?> map) {
                // Pour Google Login ou Face Login qui retournent parfois une Map
                if (map.containsKey("user")) {
                    Object userBody = map.get("user");
                    if (userBody instanceof Map<?, ?> userMap) {
                        if (userId == null) userId = (Long) userMap.get("id");
                        if (username == null) username = (String) userMap.get("username");
                    }
                } else if (map.containsKey("auth")) {
                    Object authBody = map.get("auth");
                    if (authBody instanceof LoginResponse loginResponse) {
                        if (userId == null) userId = loginResponse.getId();
                        if (username == null) username = loginResponse.getUsername();
                    }
                }
            }
        }

        // --- Cas spécifique Logout (le contexte peut être déjà nettoyé) ---
        if ("LOGOUT".equals(action) && userId == null) {
            // Le logout peut nécessiter une extraction différente si le SecurityContext est déjà vide
            // Mais généralement @AfterReturning sur logout garde encore le contexte
        }

        // On execute la sauvegarde persistante de manière asynchrone
        if ("CHANGE_PASSWORD".equals(action) && (details == null || details.isBlank())) {
            details = "Password changed successfully";
        }

        final String fUsername = username;
        final Long fUserId = userId;
        final String fDetails = details;
        
        CompletableFuture.runAsync(() -> {
            AuditLog auditLog = AuditLog.builder()
                    .userId(fUserId)
                    .username(fUsername)
                    .action(action)
                    .details(fDetails)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();
            auditLogRepository.save(auditLog);
        });
    }

    /**
     * Extraire l'IP en gérant les Proxys (Nginx, AWS ALB, etc.)
     */
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
            // Pas de proxy
            return request.getRemoteAddr();
        }
        // Proxy détécté: on prend la première IP (l'IP originale du client)
        return xfHeader.split(",")[0].trim();
    }
}
