package esprit.User.controller;

import esprit.User.dto.ActivityLogRequest;
import esprit.User.entities.AuditLog;
import esprit.User.entities.User;
import esprit.User.repositories.AuditLogRepository;
import esprit.User.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import esprit.User.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/mic1/activity")
public class ActivityLogController {


    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public ActivityLogController(AuditLogRepository auditLogRepository, UserRepository userRepository, JwtService jwtService) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }


    @PostMapping("/log")
    public ResponseEntity<?> logActivity(@RequestBody ActivityLogRequest request, HttpServletRequest httpRequest) {
        Long userId = null;
        String username = null;

        // Tenter de récupérer l'utilisateur connecté via SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
        }

        // Fallback: extraire du Token si SecurityContext est vide (ex: pendant logout)
        if (username == null || username.isBlank()) {
            String authHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    username = jwtService.extractUsername(token);
                } catch (Exception ignored) {}
            }
        }

        if (username != null && !username.isBlank()) {
            User user = userRepository.findByUsername(username);
            if (user != null) {
                userId = user.getId();
            }
        }


        // Créer l'audit log
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .username(username)
                .action(request.getAction())
                .ipAddress(getClientIp(httpRequest))
                .userAgent(request.getDevice() + " - " + request.getBrowser())
                .details(request.getDetails())
                .build();

        auditLogRepository.save(log);

        return ResponseEntity.ok(Map.of("message", "Activité enregistrée avec succès"));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentification requise"));
        }

        User user = userRepository.findByUsername(auth.getName());
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Utilisateur introuvable"));
        }

        return ResponseEntity.ok(auditLogRepository.findByUserIdOrderByTimestampDesc(user.getId()));
    }

    @GetMapping("/admin/history/{userId}")
    public ResponseEntity<?> getUserHistory(@PathVariable Long userId) {

        String username = null;
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            username = user.getUsername();
        }
        return ResponseEntity.ok(auditLogRepository.findByUserIdOrUsernameOrderByTimestampDesc(userId, username));
    }



    private String getClientIp(HttpServletRequest request) {

        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
