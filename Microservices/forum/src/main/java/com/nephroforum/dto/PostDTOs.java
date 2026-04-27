package com.nephroforum.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

public class PostDTOs {

    public record CreatePostRequest(
            String title,
            String description,
            List<String> tags,
            String authorName,
            boolean anonymous
    ) {}

    public record UpdatePostRequest(
            String title,
            String description,
            List<String> tags
    ) {}

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PostResponse {
        private Long id;
        private String title;
        private String description;
        private String imageUrl;
        private String authorName;
        private List<String> tags;
        private Map<String, Long> reactions;
        private long commentCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean anonymous;
        private String displayName;
        private long views;
        private boolean hasDoctorResponse;
        private boolean pinned;
        private String pinnedBy;
        private LocalDateTime pinnedAt;
    }
}