package com.nephroforum.repository;

import com.nephroforum.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientNameOrderByCreatedAtDesc(String recipientName);

    long countByRecipientNameAndReadFalse(String recipientName);
}