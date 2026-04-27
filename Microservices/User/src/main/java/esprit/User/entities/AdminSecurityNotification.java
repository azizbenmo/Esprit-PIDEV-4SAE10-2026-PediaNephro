package esprit.User.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_security_notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSecurityNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String type;

    @Column(nullable = false, length = 512)
    private String message;

    @Column(nullable = false, length = 64)
    private String clientIp;

    @Column(length = 400)
    private String userAgentSnippet;

    /** Extrait non sensible du champ identifiant (jamais le mot de passe). */
    @Column(length = 120)
    private String usernameHint;

    @Column(name = "seen_by_admin", nullable = false)
    private boolean seenByAdmin;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
