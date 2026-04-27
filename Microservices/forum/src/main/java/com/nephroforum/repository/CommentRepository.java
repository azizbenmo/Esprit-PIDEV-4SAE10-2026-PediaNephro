package com.nephroforum.repository;

import com.nephroforum.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    long countByAuthorName(String authorName);

    List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);

    @Query("SELECT c FROM Comment c WHERE c.authorName = :authorName AND c.moderated = false ORDER BY c.createdAt DESC")
    List<Comment> findByAuthorName(@Param("authorName") String authorName);

    long countByModeratedTrue();
}