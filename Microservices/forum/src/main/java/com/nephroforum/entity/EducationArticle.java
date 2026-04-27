package com.nephroforum.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "education_articles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EducationArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArticleType type;

    private String youtubeUrl;
    private String imageUrl;
    private String pdfUrl;

    @Column(nullable = false)
    private String authorName;

    @Builder.Default
    private boolean validated = true;

    @Builder.Default
    private int views = 0;

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ArticleType {
        ARTICLE, VIDEO, GUIDE, IMAGE
    }
}