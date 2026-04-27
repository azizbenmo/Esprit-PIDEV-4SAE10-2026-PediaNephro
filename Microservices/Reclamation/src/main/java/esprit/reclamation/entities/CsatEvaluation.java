package esprit.reclamation.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "csat_evaluation",
        uniqueConstraints = {@UniqueConstraint(name = "uk_csat_reclamation", columnNames = "reclamation_id")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsatEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reclamation_id", nullable = false, unique = true)
    private Long reclamationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer note;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_evaluation", nullable = false, updatable = false)
    private LocalDateTime dateEvaluation;

    @PrePersist
    protected void onCreate() {
        if (dateEvaluation == null) {
            dateEvaluation = LocalDateTime.now();
        }
    }
}
