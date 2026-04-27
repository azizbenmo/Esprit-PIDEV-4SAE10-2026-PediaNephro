package com.nephroforum.repository;

import com.nephroforum.entity.EducationArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EducationRepository extends JpaRepository<EducationArticle, Long> {
    List<EducationArticle> findByValidatedTrueOrderByCreatedAtDesc();
    List<EducationArticle> findByCategoryAndValidatedTrueOrderByCreatedAtDesc(String category);
    List<EducationArticle> findByTypeAndValidatedTrueOrderByCreatedAtDesc(EducationArticle.ArticleType type);
    List<EducationArticle> findByAuthorNameOrderByCreatedAtDesc(String authorName);
    List<EducationArticle> findByTitleContainingIgnoreCaseAndValidatedTrue(String keyword);
}