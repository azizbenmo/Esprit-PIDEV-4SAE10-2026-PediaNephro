package com.nephroforum.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "podcasts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Podcast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String audioUrl;

    @Column(columnDefinition = "TEXT")
    private String transcription;

    @Column(nullable = false)
    private String authorName;

    private String coverUrl;

    @Builder.Default
    private int durationSeconds = 0;

    @Builder.Default
    private int plays = 0;

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}