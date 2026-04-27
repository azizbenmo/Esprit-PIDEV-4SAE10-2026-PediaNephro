package com.nephroforum.repository;

import com.nephroforum.entity.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
    List<GroupChat> findAllByOrderByCreatedAtAsc();
}