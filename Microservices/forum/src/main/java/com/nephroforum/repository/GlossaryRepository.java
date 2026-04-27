package com.nephroforum.repository;

import com.nephroforum.entity.GlossaryTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GlossaryRepository extends JpaRepository<GlossaryTerm, Long> {

    Optional<GlossaryTerm> findByTermIgnoreCase(String term);

    List<GlossaryTerm> findByCategoryOrderByTermAsc(String category);

    @Query("""
        SELECT g FROM GlossaryTerm g
        WHERE LOWER(g.term) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY g.term ASC
        """)
    List<GlossaryTerm> search(@Param("keyword") String keyword);

    // Détecter les termes médicaux dans un texte
    @Query("""
        SELECT g FROM GlossaryTerm g
        WHERE LOWER(:text) LIKE LOWER(CONCAT('%', g.term, '%'))
        """)
    List<GlossaryTerm> findTermsInText(@Param("text") String text);
}