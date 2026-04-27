package com.nephroforum.service;

import com.nephroforum.dto.PodcastDTOs;
import com.nephroforum.entity.Podcast;
import com.nephroforum.repository.PodcastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PodcastService {

    private final PodcastRepository podcastRepo;

    public List<PodcastDTOs.PodcastResponse> getAll() {
        return podcastRepo.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<PodcastDTOs.PodcastResponse> getByCategory(String category) {
        return podcastRepo.findByCategoryOrderByCreatedAtDesc(category)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<PodcastDTOs.PodcastResponse> search(String keyword) {
        return podcastRepo.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<PodcastDTOs.PodcastResponse> getByAuthor(String authorName) {
        return podcastRepo.findByAuthorName(authorName)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PodcastDTOs.PodcastResponse create(PodcastDTOs.CreatePodcastRequest req) {
        Podcast podcast = Podcast.builder()
                .title(req.title())
                .description(req.description())
                .category(req.category())
                .audioUrl(req.audioUrl())
                .transcription(req.transcription())
                .authorName(req.authorName())
                .coverUrl(req.coverUrl())
                .durationSeconds(req.durationSeconds())
                .build();
        return toResponse(podcastRepo.save(podcast));
    }

    @Transactional
    public PodcastDTOs.PodcastResponse update(Long id, PodcastDTOs.UpdatePodcastRequest req) {
        Podcast p = podcastRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Podcast not found"));
        p.setTitle(req.title());
        p.setDescription(req.description());
        p.setCategory(req.category());
        if (req.transcription() != null) p.setTranscription(req.transcription());
        if (req.coverUrl() != null) p.setCoverUrl(req.coverUrl());
        return toResponse(podcastRepo.save(p));
    }

    @Transactional
    public void incrementPlays(Long id) {
        podcastRepo.findById(id).ifPresent(p -> {
            p.setPlays(p.getPlays() + 1);
            podcastRepo.save(p);
        });
    }

    @Transactional
    public void delete(Long id) {
        podcastRepo.deleteById(id);
    }

    private PodcastDTOs.PodcastResponse toResponse(Podcast p) {
        return PodcastDTOs.PodcastResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .category(p.getCategory())
                .audioUrl(p.getAudioUrl())
                .transcription(p.getTranscription())
                .authorName(p.getAuthorName())
                .coverUrl(p.getCoverUrl())
                .durationSeconds(p.getDurationSeconds())
                .plays(p.getPlays())
                .createdAt(p.getCreatedAt())
                .build();
    }
}