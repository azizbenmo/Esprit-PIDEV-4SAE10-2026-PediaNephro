package com.nephroforum.dto;

import lombok.Builder;
import java.time.LocalDateTime;

public class PodcastDTOs {

    public record CreatePodcastRequest(
            String title,
            String description,
            String category,
            String audioUrl,
            String transcription,
            String authorName,
            String coverUrl,
            int durationSeconds
    ) {}

    public record UpdatePodcastRequest(
            String title,
            String description,
            String category,
            String transcription,
            String coverUrl
    ) {}

    @Builder
    public record PodcastResponse(
            Long id,
            String title,
            String description,
            String category,
            String audioUrl,
            String transcription,
            String authorName,
            String coverUrl,
            int durationSeconds,
            int plays,
            LocalDateTime createdAt
    ) {}
}