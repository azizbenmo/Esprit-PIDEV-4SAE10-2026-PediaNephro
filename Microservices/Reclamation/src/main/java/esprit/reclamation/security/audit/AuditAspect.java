package esprit.reclamation.security.audit;

import esprit.reclamation.entities.AuditLog;
import esprit.reclamation.repositories.AuditLogRepository;
import esprit.reclamation.dto.ReclamationRequestDTO;
import esprit.reclamation.dto.ReponseAdminDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.CompletableFuture;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @AfterReturning("@annotation(logAction)")
    public void logAfterMethodCall(JoinPoint joinPoint, LogAction logAction) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return;
        HttpServletRequest request = attributes.getRequest();

        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        final String action = logAction.action();
        
        Long userId = null;

        // On essaie d'extraire l'ID utilisateur depuis les arguments de la méthode
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof ReclamationRequestDTO) {
                userId = ((ReclamationRequestDTO) arg).getUserId();
                break;
            } else if (arg instanceof ReponseAdminDTO) {
                userId = ((ReponseAdminDTO) arg).getAdminId();
                break;
            }
        }
        // L'ajout dans la base (CREATE_RECLAMATION) doit être anonyme
        if ("CREATE_RECLAMATION".equals(action)) {
            userId = null;
        }

        final Long fUserId = userId;
        
        CompletableFuture.runAsync(() -> {
            AuditLog auditLog = AuditLog.builder()
                    .userId(fUserId)
                    .action(action)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();
            auditLogRepository.save(auditLog);
        });
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
