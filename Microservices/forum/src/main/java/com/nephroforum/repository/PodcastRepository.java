package com.nephroforum.repository;

import com.nephroforum.entity.Podcast;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PodcastRepository extends JpaRepository<Podcast, Long> {
    List<Podcast> findAllByOrderByCreatedAtDesc();
    List<Podcast> findByCategoryOrderByCreatedAtDesc(String category);
    List<Podcast> findByAuthorNameOrderByCreatedAtDesc(String authorName);
    List<Podcast> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String desc);
    List<Podcast> findByAuthorName(String authorName);

}