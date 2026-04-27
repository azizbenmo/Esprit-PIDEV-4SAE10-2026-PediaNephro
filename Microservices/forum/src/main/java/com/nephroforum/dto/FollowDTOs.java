package com.nephroforum.dto;

import com.nephroforum.entity.Follow.FollowStatus;
import lombok.Builder;
import java.time.LocalDateTime;

public class FollowDTOs {

    public record FollowRequest(String followerName, String followingName) {}

    public record RespondRequest(String responderName, boolean accept) {}

    @Builder
    public record FollowResponse(
            Long id,
            String followerName,
            String followingName,
            FollowStatus status,
            LocalDateTime createdAt,
            LocalDateTime respondedAt
    ) {}
}