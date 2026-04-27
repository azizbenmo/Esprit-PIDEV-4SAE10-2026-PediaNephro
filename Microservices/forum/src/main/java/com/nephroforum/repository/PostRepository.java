package com.nephroforum.repository;

import com.nephroforum.entity.Post;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    long countByAuthorName(String authorName);

    // ── RECENT — épinglés en premier, puis date DESC ──────────────────────────
    @Query(value = """
        SELECT DISTINCT p.* FROM posts p
        LEFT JOIN post_tags pt ON pt.post_id = p.id
        LEFT JOIN tags t ON t.id = pt.tag_id
        WHERE p.deleted = false
        AND (:keyword = ''
            OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY p.pinned DESC, p.created_at DESC
        """,
            countQuery = """
        SELECT COUNT(DISTINCT p.id) FROM posts p
        LEFT JOIN post_tags pt ON pt.post_id = p.id
        LEFT JOIN tags t ON t.id = pt.tag_id
        WHERE p.deleted = false
        AND (:keyword = ''
            OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """,
            nativeQuery = true)
    Page<Post> searchPostsRecent(@Param("keyword") String keyword, Pageable pageable);

    // ── OLDEST — épinglés en premier, puis date ASC ───────────────────────────
    @Query(value = """
        SELECT DISTINCT p.* FROM posts p
        LEFT JOIN post_tags pt ON pt.post_id = p.id
        LEFT JOIN tags t ON t.id = pt.tag_id
        WHERE p.deleted = false
        AND (:keyword = ''
            OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY p.pinned DESC, p.created_at ASC
        """,
            countQuery = """
        SELECT COUNT(DISTINCT p.id) FROM posts p
        LEFT JOIN post_tags pt ON pt.post_id = p.id
        LEFT JOIN tags t ON t.id = pt.tag_id
        WHERE p.deleted = false
        AND (:keyword = ''
            OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """,
            nativeQuery = true)
    Page<Post> searchPostsOldest(@Param("keyword") String keyword, Pageable pageable);

    // ── HOT — épinglés en premier, puis nb commentaires DESC ─────────────────
    @Query(value = """
        SELECT p.* FROM posts p
        LEFT JOIN comments c ON c.post_id = p.id AND c.moderated = false
        WHERE p.deleted = false
        AND (:keyword = ''
            OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        GROUP BY p.id
        ORDER BY p.pinned DESC, COUNT(c.id) DESC
        """,
            countQuery = """
        SELECT COUNT(*) FROM posts WHERE deleted = false
        """,
            nativeQuery = true)
    Page<Post> findHotPosts(@Param("keyword") String keyword, Pageable pageable);

    // ── Recherche avancée ─────────────────────────────────────────────────────
    @Query("""
        SELECT DISTINCT p FROM Post p
        LEFT JOIN p.tags t
        WHERE p.deleted = false

        AND (:keyword IS NULL OR :keyword = ''
            OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))

        AND (:tag IS NULL OR :tag = ''
            OR LOWER(t.name) = LOWER(:tag))

        AND (:dateFrom IS NULL
            OR p.createdAt >= :dateFrom)

        AND (:dateTo IS NULL
            OR p.createdAt <= :dateTo)

        AND (:hasImage IS NULL
            OR (:hasImage = true AND p.imageUrl IS NOT NULL)
            OR (:hasImage = false AND p.imageUrl IS NULL))

        AND (:anonymous IS NULL
            OR p.anonymous = :anonymous)

        ORDER BY p.pinned DESC, p.createdAt DESC
        """)
    Page<Post> advancedSearch(
            @Param("keyword")   String keyword,
            @Param("tag")       String tag,
            @Param("dateFrom")  LocalDateTime dateFrom,
            @Param("dateTo")    LocalDateTime dateTo,
            @Param("hasImage")  Boolean hasImage,
            @Param("anonymous") Boolean anonymous,
            Pageable pageable
    );

    // ── Posts sans réponse depuis plus de 24h ─────────────────────────────────
    @Query("""
        SELECT p FROM Post p
        WHERE p.deleted = false
        AND p.createdAt <= :threshold
        AND SIZE(p.comments) = 0
        """)
    List<Post> findUnansweredPostsOlderThan(@Param("threshold") LocalDateTime threshold);

    // ── Posts par auteur (sauf un id) ─────────────────────────────────────────
    @Query("""
        SELECT p FROM Post p
        WHERE p.authorName = :authorName
        AND p.id != :excludeId
        AND p.deleted = false
        ORDER BY p.createdAt DESC
        """)
    List<Post> findByAuthorNameAndIdNot(
            @Param("authorName") String authorName,
            @Param("excludeId")  Long excludeId
    );

    long countByDeletedFalse();

    long countByAnonymousTrue();
}