package com.nephroforum.dto;

import com.nephroforum.entity.EducationArticle.ArticleType;
import lombok.Builder;
import java.time.LocalDateTime;

public class EducationDTOs {

    public record CreateArticleRequest(
            String title,
            String content,
            String category,
            ArticleType type,
            String youtubeUrl,
            String imageUrl,
            String pdfUrl,
            String authorName
    ) {}

    public record UpdateArticleRequest(
            String title,
            String content,
            String category,
            String youtubeUrl,
            String imageUrl,
            String pdfUrl
    ) {}

    @Builder
    public record ArticleResponse(
            Long id,
            String title,
            String content,
            String category,
            ArticleType type,
            String youtubeUrl,
            String imageUrl,
            String pdfUrl,
            String authorName,
            boolean validated,
            int views,
            LocalDateTime createdAt
    ) {}
}