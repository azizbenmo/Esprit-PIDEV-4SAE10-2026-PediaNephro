package com.nephroforum.repository;

import com.nephroforum.entity.Reaction;
import com.nephroforum.entity.Reaction.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    @Query("SELECT r.reactionType, COUNT(r) FROM Reaction r WHERE r.post.id = :postId GROUP BY r.reactionType")
    List<Object[]> countByPostId(@Param("postId") Long postId);

    @Query("SELECT r.reactionType, COUNT(r) FROM Reaction r WHERE r.comment.id = :commentId GROUP BY r.reactionType")
    List<Object[]> countByCommentId(@Param("commentId") Long commentId);

    // New
    List<Reaction> findByPostId(Long postId);
    List<Reaction> findByCommentId(Long commentId);
}
