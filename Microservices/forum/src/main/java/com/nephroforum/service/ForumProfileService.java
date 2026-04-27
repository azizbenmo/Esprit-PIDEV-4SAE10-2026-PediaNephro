package com.nephroforum.service;

import com.nephroforum.dto.ForumProfileDTOs;
import com.nephroforum.entity.ForumProfile;
import com.nephroforum.repository.ForumProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ForumProfileService {

    private final ForumProfileRepository profileRepo;

    public ForumProfileDTOs.ProfileResponse getOrCreate(String username) {
        ForumProfile profile = profileRepo.findByUsername(username)
                .orElseGet(() -> {
                    ForumProfile.Role role = username.startsWith("Dr.")
                            ? ForumProfile.Role.DOCTOR
                            : username.equals("Admin")
                            ? ForumProfile.Role.ADMIN
                            : ForumProfile.Role.PATIENT;

                    return profileRepo.save(ForumProfile.builder()
                            .username(username)
                            .role(role)
                            .build());
                });
        return toResponse(profile);
    }

    @Transactional
    public ForumProfileDTOs.ProfileResponse upsert(ForumProfileDTOs.UpsertProfileRequest req) {
        ForumProfile profile = profileRepo.findByUsername(req.username())
                .orElseGet(() -> ForumProfile.builder()
                        .username(req.username())
                        .build());

        if (req.role()           != null) profile.setRole(req.role());
        if (req.bio()            != null) profile.setBio(req.bio());
        if (req.ville()          != null) profile.setVille(req.ville());
        if (req.avatarUrl()      != null) profile.setAvatarUrl(req.avatarUrl());
        if (req.coverUrl()       != null) profile.setCoverUrl(req.coverUrl());
        if (req.specialite()     != null) profile.setSpecialite(req.specialite());
        if (req.hopital()        != null) profile.setHopital(req.hopital());
        if (req.diplomes()       != null) profile.setDiplomes(req.diplomes());
        if (req.childName()      != null) profile.setChildName(req.childName());
        if (req.childAge()       != null) profile.setChildAge(req.childAge());
        if (req.childDiagnosis() != null) profile.setChildDiagnosis(req.childDiagnosis());
        if (req.parentRelation() != null) profile.setParentRelation(req.parentRelation());

        profile.setUpdatedAt(LocalDateTime.now());
        return toResponse(profileRepo.save(profile));
    }

    private ForumProfileDTOs.ProfileResponse toResponse(ForumProfile p) {
        return ForumProfileDTOs.ProfileResponse.builder()
                .id(p.getId())
                .username(p.getUsername())
                .role(p.getRole())
                .bio(p.getBio())
                .ville(p.getVille())
                .avatarUrl(p.getAvatarUrl())
                .coverUrl(p.getCoverUrl())
                .specialite(p.getSpecialite())
                .hopital(p.getHopital())
                .diplomes(p.getDiplomes())
                .childName(p.getChildName())
                .childAge(p.getChildAge())
                .childDiagnosis(p.getChildDiagnosis())
                .parentRelation(p.getParentRelation())
                .followersCount(p.getFollowersCount())
                .followingCount(p.getFollowingCount())
                .createdAt(p.getCreatedAt())
                .build();
    }
}