package com.pedianephro.subscription.repository;

import com.pedianephro.subscription.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadStatusFalse(Long userId);

    @Modifying
    @Query("update Notification n set n.readStatus = true where n.userId = :userId and n.readStatus = false and n.title = :title")
    int markUnreadByTitleAsRead(@Param("userId") Long userId, @Param("title") String title);
}
