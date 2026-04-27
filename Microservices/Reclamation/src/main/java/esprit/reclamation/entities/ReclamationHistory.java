package esprit.reclamation.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reclamation_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReclamationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reclamation_id", nullable = false)
    private Long reclamationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ancien_statut")
    private StatutReclamation ancienStatut;

    @Enumerated(EnumType.STRING)
    @Column(name = "nouveau_statut", nullable = false)
    private StatutReclamation nouveauStatut;

    @Column(name = "acteur_id")
    private Long acteurId;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_changement", nullable = false, updatable = false)
    private LocalDateTime dateChangement;

    @PrePersist
    protected void onCreate() {
        if (dateChangement == null) {
            dateChangement = LocalDateTime.now();
        }
    }
}
