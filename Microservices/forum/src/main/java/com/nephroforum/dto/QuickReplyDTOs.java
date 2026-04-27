package com.nephroforum.dto;

import lombok.*;

public class QuickReplyDTOs {

    public record CreateQuickReplyRequest(
            String label,
            String content,
            String ownerName
    ) {}

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class QuickReplyResponse {
        private Long id;
        private String label;
        private String content;
        private String ownerName;
    }
}