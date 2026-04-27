package com.nephroforum.repository;

import com.nephroforum.entity.BannedWord;
import com.nephroforum.entity.BannedWord.WordType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BannedWordRepository extends JpaRepository<BannedWord, Long> {
    List<BannedWord> findByType(WordType type);
    Optional<BannedWord> findByWordAndType(String word, WordType type);
    boolean existsByWordAndType(String word, WordType type);
    void deleteByWordAndType(String word, WordType type);
}