package com.nephroforum.repository;

import com.nephroforum.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Récupère la conversation entre deux personnes
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE (m.senderName = :user1 AND m.receiverName = :user2)
           OR (m.senderName = :user2 AND m.receiverName = :user1)
        ORDER BY m.sentAt ASC
        """)
    List<ChatMessage> findConversation(
            @Param("user1") String user1,
            @Param("user2") String user2
    );

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiverName = :receiver AND m.senderName = :sender AND m.seen = false")
    long countUnread(@Param("receiver") String receiver, @Param("sender") String sender);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.seen = true WHERE m.receiverName = :receiver AND m.senderName = :sender")
    void markAllSeen(@Param("receiver") String receiver, @Param("sender") String sender);

    @Query("SELECT m FROM ChatMessage m WHERE m.id = :id")
    Optional<ChatMessage> findById(@Param("id") Long id);
}