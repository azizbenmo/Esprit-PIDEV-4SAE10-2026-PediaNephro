package com.nephroforum.repository;

import com.nephroforum.entity.ForumProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ForumProfileRepository extends JpaRepository<ForumProfile, Long> {
    Optional<ForumProfile> findByUsername(String username);
    boolean existsByUsername(String username);
}