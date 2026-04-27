package esprit.User.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;        // Peut être null si l'action est anonyme
    private String username;    // Fallback si l'ID n'est pas trouvable

    @Column(nullable = false, length = 100)
    private String action;      // L'action métier: LOGIN, CREATE_USER...

    @Column(nullable = false, length = 45)
    private String ipAddress;   // Prend en charge IPv4 ("127.0.0.1") et IPv6 format

    @Column(length = 255)
    private String userAgent;   // Le Device / Navigateur

    @Lob
    @Column(columnDefinition = "TEXT")
    private String details;     // Détails optionnels (Ids modifiés, erreurs...)

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void onPrePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
