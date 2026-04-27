package com.nephroforum.repository;

import com.nephroforum.entity.QuickReply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuickReplyRepository extends JpaRepository<QuickReply, Long> {
    List<QuickReply> findByOwnerName(String ownerName);
}