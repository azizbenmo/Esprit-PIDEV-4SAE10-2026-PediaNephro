package com.pedianephro.subscription.repository;

import com.pedianephro.subscription.entity.UserBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {
    Optional<UserBehavior> findByUserId(Long userId);
}
