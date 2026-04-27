package com.nephroforum.service;

import com.nephroforum.dto.EducationDTOs;
import com.nephroforum.entity.EducationArticle;
import com.nephroforum.exception.ResourceNotFoundException;
import com.nephroforum.repository.EducationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EducationService {

    private final EducationRepository educationRepo;

    public List<EducationDTOs.ArticleResponse> getAll() {
        return educationRepo.findByValidatedTrueOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<EducationDTOs.ArticleResponse> getByCategory(String category) {
        return educationRepo.findByCategoryAndValidatedTrueOrderByCreatedAtDesc(category)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<EducationDTOs.ArticleResponse> getByType(EducationArticle.ArticleType type) {
        return educationRepo.findByTypeAndValidatedTrueOrderByCreatedAtDesc(type)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<EducationDTOs.ArticleResponse> search(String keyword) {
        return educationRepo.findByTitleContainingIgnoreCaseAndValidatedTrue(keyword)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<EducationDTOs.ArticleResponse> getByAuthor(String authorName) {
        return educationRepo.findByAuthorNameOrderByCreatedAtDesc(authorName)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public EducationDTOs.ArticleResponse create(EducationDTOs.CreateArticleRequest req) {
        EducationArticle article = EducationArticle.builder()
                .title(req.title())
                .content(req.content())
                .category(req.category())
                .type(req.type())
                .youtubeUrl(req.youtubeUrl())
                .imageUrl(req.imageUrl())
                .pdfUrl(req.pdfUrl())
                .authorName(req.authorName())
                .build();
        return toResponse(educationRepo.save(article));
    }

    @Transactional
    public EducationDTOs.ArticleResponse update(Long id, EducationDTOs.UpdateArticleRequest req) {
        EducationArticle article = educationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found: " + id));
        article.setTitle(req.title());
        article.setContent(req.content());
        article.setCategory(req.category());
        if (req.youtubeUrl() != null) article.setYoutubeUrl(req.youtubeUrl());
        if (req.imageUrl() != null) article.setImageUrl(req.imageUrl());
        if (req.pdfUrl() != null) article.setPdfUrl(req.pdfUrl());
        return toResponse(educationRepo.save(article));
    }

    @Transactional
    public void incrementViews(Long id) {
        educationRepo.findById(id).ifPresent(a -> {
            a.setViews(a.getViews() + 1);
            educationRepo.save(a);
        });
    }

    @Transactional
    public void delete(Long id) {
        educationRepo.deleteById(id);
    }

    private EducationDTOs.ArticleResponse toResponse(EducationArticle a) {
        return EducationDTOs.ArticleResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .category(a.getCategory())
                .type(a.getType())
                .youtubeUrl(a.getYoutubeUrl())
                .imageUrl(a.getImageUrl())
                .pdfUrl(a.getPdfUrl())
                .authorName(a.getAuthorName())
                .validated(a.isValidated())
                .views(a.getViews())
                .createdAt(a.getCreatedAt())
                .build();
    }
}