package com.nephroforum.repository;

import com.nephroforum.entity.Ban;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BanRepository extends JpaRepository<Ban, Long> {
    Optional<Ban> findByUsername(String username);
}