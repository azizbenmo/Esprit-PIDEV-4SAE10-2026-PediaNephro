package com.nephroforum.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

public class CommentDTOs {

    public record CreateCommentRequest(
            String content,
            String authorName,
            Long parentId,
            Boolean anonymous
    ) {}

    public record UpdateCommentRequest(
            String content
    ) {}

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CommentResponse {
        private Long id;
        private String content;
        private String authorName;
        private Map<String, Long> reactions;
        private List<CommentResponse> replies;
        private boolean moderated;
        private LocalDateTime createdAt;
        private boolean anonymous;
        private String displayName;
        private boolean officialResponse;
    }
}