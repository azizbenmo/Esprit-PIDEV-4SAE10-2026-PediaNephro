package com.nephroforum.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "follows",
        uniqueConstraints = @UniqueConstraint(columnNames = {"follower_name", "following_name"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String followerName;   // celui qui suit

    @Column(nullable = false)
    private String followingName;  // celui qui est suivi

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FollowStatus status = FollowStatus.PENDING;

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime respondedAt;

    public enum FollowStatus {
        PENDING,   // demande envoyée
        ACCEPTED,  // acceptée
        REJECTED   // rejetée
    }
}