package com.nephroforum.dto;

import com.nephroforum.entity.ForumProfile.Role;
import lombok.Builder;
import java.time.LocalDateTime;

public class ForumProfileDTOs {

    public record UpsertProfileRequest(
            String username,
            Role role,
            String bio,
            String ville,
            String avatarUrl,
            String coverUrl,
            // Médecin
            String specialite,
            String hopital,
            String diplomes,
            // Patient
            String childName,
            Integer childAge,
            String childDiagnosis,
            String parentRelation
    ) {}

    @Builder
    public record ProfileResponse(
            Long id,
            String username,
            Role role,
            String bio,
            String ville,
            String avatarUrl,
            String coverUrl,
            String specialite,
            String hopital,
            String diplomes,
            String childName,
            Integer childAge,
            String childDiagnosis,
            String parentRelation,
            int followersCount,
            int followingCount,
            LocalDateTime createdAt
    ) {}
}