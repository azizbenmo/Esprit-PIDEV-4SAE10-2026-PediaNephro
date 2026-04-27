package com.nephroforum.repository;

import com.nephroforum.entity.PostView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostViewRepository extends JpaRepository<PostView, Long> {
    boolean existsByPostIdAndViewerName(Long postId, String viewerName);
}