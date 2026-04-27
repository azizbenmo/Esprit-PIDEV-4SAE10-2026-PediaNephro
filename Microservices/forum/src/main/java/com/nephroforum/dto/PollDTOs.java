package com.nephroforum.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class PollDTOs {

    public record CreatePollRequest(
            String question,
            String createdBy,
            List<String> options,
            LocalDateTime expiresAt
    ) {}

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PollResponse {
        private Long id;
        private String question;
        private String createdBy;
        private boolean active;
        private List<OptionResponse> options;
        private int totalVotes;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private String userVotedOptionId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OptionResponse {
        private Long id;
        private String text;
        private int votes;
        private double percentage;
    }

    public record UpdatePollRequest(
            String question,
            List<String> options,
            LocalDateTime expiresAt
    ) {}
}