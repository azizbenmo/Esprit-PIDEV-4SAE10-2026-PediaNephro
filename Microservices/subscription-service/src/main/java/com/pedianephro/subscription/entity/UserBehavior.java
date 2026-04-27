package com.pedianephro.subscription.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_behavior")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehavior {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Builder.Default
    @Column(name = "jours_sans_connexion", nullable = false)
    private Integer joursSansConnexion = 0;

    @Builder.Default
    @Column(name = "bilans_en_retard", nullable = false)
    private Integer bilansEnRetard = 0;

    @Builder.Default
    @Column(name = "rappels_ignores", nullable = false)
    private Integer rappelsIgnores = 0;

    @Builder.Default
    @Column(name = "rendez_vous_annules", nullable = false)
    private Integer rendezVousAnnules = 0;

    @Builder.Default
    @Column(name = "medicaments_non_confirmes", nullable = false)
    private Integer medicamentsNonConfirmes = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
