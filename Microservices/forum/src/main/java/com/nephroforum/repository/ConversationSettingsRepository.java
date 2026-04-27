package com.nephroforum.repository;

import com.nephroforum.entity.ConversationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConversationSettingsRepository
        extends JpaRepository<ConversationSettings, Long> {
    Optional<ConversationSettings> findByConversationKey(String key);
}