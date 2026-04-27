package com.nephroforum.repository;

import com.nephroforum.entity.Badge;
import com.nephroforum.entity.Badge.BadgeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    List<Badge> findByOwnerName(String ownerName);

    Optional<Badge> findByOwnerNameAndType(String ownerName, BadgeType type);

    boolean existsByOwnerNameAndType(String ownerName, BadgeType type);
}