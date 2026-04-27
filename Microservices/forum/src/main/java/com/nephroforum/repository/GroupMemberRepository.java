package com.nephroforum.repository;

import com.nephroforum.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);
    boolean existsByGroupIdAndUsername(Long groupId, String username);
    void deleteByGroupIdAndUsername(Long groupId, String username);
    long countByGroupId(Long groupId);
}