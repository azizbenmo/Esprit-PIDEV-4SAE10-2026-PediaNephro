package com.nephroforum.dto;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

public class GroupChatDTOs {

    public record CreateGroupRequest(
            String name,
            String topic,
            String emoji,
            String createdBy
    ) {}

    @Builder
    public record GroupResponse(
            Long id,
            String name,
            String topic,
            String emoji,
            String createdBy,
            long memberCount,
            boolean isMember,
            LocalDateTime createdAt
    ) {}

    public record SendMessageRequest(
            String authorName,
            String content
    ) {}

    @Builder
    public record MessageResponse(
            Long id,
            Long groupId,
            String authorName,
            String content,
            Object reactions,
            LocalDateTime createdAt
    ) {}

    @Builder
    public record MemberResponse(
            String username,
            boolean isDoctor,
            LocalDateTime joinedAt
    ) {}
}