package com.nephroforum.dto;

import com.nephroforum.entity.Badge.BadgeType;
import lombok.*;
import java.time.LocalDateTime;

public class BadgeDTOs {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BadgeResponse {
        private Long id;
        private String ownerName;
        private BadgeType type;
        private String label;
        private String emoji;
        private LocalDateTime earnedAt;
    }

    public record AwardBadgeRequest(
            String ownerName,
            String type,
            String awardedBy
    ) {}
}