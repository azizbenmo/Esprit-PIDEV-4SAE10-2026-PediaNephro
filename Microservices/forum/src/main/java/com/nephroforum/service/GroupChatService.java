package com.nephroforum.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nephroforum.dto.GroupChatDTOs;
import com.nephroforum.entity.GroupChat;
import com.nephroforum.entity.GroupMember;
import com.nephroforum.entity.GroupMessage;
import com.nephroforum.exception.BadRequestException;
import com.nephroforum.repository.GroupChatRepository;
import com.nephroforum.repository.GroupMemberRepository;
import com.nephroforum.repository.GroupMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final GroupChatRepository groupRepo;
    private final GroupMessageRepository messageRepo;
    private final GroupMemberRepository memberRepo;
    private final ObjectMapper objectMapper;

    // ── Groupes ───────────────────────────────────────────────────────────────
    public List<GroupChatDTOs.GroupResponse> getAllGroups(String username) {
        return groupRepo.findAllByOrderByCreatedAtAsc().stream()
                .map(g -> toGroupResponse(g, username))
                .collect(Collectors.toList());
    }

    @Transactional
    public GroupChatDTOs.GroupResponse createGroup(GroupChatDTOs.CreateGroupRequest req) {
        GroupChat group = GroupChat.builder()
                .name(req.name())
                .topic(req.topic())
                .emoji(req.emoji())
                .createdBy(req.createdBy())
                .build();
        group = groupRepo.save(group);

        // Créateur rejoint automatiquement
        memberRepo.save(GroupMember.builder()
                .groupId(group.getId())
                .username(req.createdBy())
                .build());

        return toGroupResponse(group, req.createdBy());
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        memberRepo.findByGroupId(groupId)
                .forEach(m -> memberRepo.deleteByGroupIdAndUsername(groupId, m.getUsername()));
        messageRepo.findByGroupIdOrderByCreatedAtAsc(groupId)
                .forEach(messageRepo::delete);
        groupRepo.deleteById(groupId);
    }

    // ── Membres ───────────────────────────────────────────────────────────────
    @Transactional
    public void joinGroup(Long groupId, String username) {
        if (memberRepo.existsByGroupIdAndUsername(groupId, username))
            throw new BadRequestException("Vous êtes déjà membre de ce groupe.");
        memberRepo.save(GroupMember.builder()
                .groupId(groupId).username(username).build());
    }

    @Transactional
    public void leaveGroup(Long groupId, String username) {
        memberRepo.deleteByGroupIdAndUsername(groupId, username);
    }

    public List<GroupChatDTOs.MemberResponse> getMembers(Long groupId) {
        return memberRepo.findByGroupId(groupId).stream()
                .map(m -> GroupChatDTOs.MemberResponse.builder()
                        .username(m.getUsername())
                        .isDoctor(m.getUsername().startsWith("Dr."))
                        .joinedAt(m.getJoinedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ── Messages ──────────────────────────────────────────────────────────────
    public List<GroupChatDTOs.MessageResponse> getMessages(Long groupId, String username) {
        if (!memberRepo.existsByGroupIdAndUsername(groupId, username))
            throw new BadRequestException("Vous devez rejoindre ce groupe pour lire les messages.");
        return messageRepo.findByGroupIdOrderByCreatedAtAsc(groupId).stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public GroupChatDTOs.MessageResponse sendMessage(Long groupId, GroupChatDTOs.SendMessageRequest req) {
        if (!memberRepo.existsByGroupIdAndUsername(groupId, req.authorName()))
            throw new BadRequestException("Vous devez rejoindre ce groupe pour envoyer un message.");
        GroupMessage msg = GroupMessage.builder()
                .groupId(groupId)
                .authorName(req.authorName())
                .content(req.content())
                .build();
        return toMessageResponse(messageRepo.save(msg));
    }

    @Transactional
    public GroupChatDTOs.MessageResponse reactToMessage(Long messageId, String username, String emoji) {
        GroupMessage msg = messageRepo.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        try {
            Map<String, Object> reactions = msg.getReactions() != null && !msg.getReactions().equals("{}")
                    ? objectMapper.readValue(msg.getReactions(), Map.class)
                    : new HashMap<>();

            String key = emoji + "_" + username;
            if (reactions.containsKey(key)) {
                reactions.remove(key);
            } else {
                reactions.put(key, emoji);
            }
            msg.setReactions(objectMapper.writeValueAsString(reactions));
            return toMessageResponse(messageRepo.save(msg));
        } catch (Exception e) {
            return toMessageResponse(msg);
        }
    }

    @Transactional
    public void deleteMessage(Long messageId, String username) {
        GroupMessage msg = messageRepo.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        if (!msg.getAuthorName().equals(username))
            throw new BadRequestException("Vous ne pouvez supprimer que vos propres messages.");
        messageRepo.delete(msg);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private GroupChatDTOs.GroupResponse toGroupResponse(GroupChat g, String username) {
        return GroupChatDTOs.GroupResponse.builder()
                .id(g.getId())
                .name(g.getName())
                .topic(g.getTopic())
                .emoji(g.getEmoji())
                .createdBy(g.getCreatedBy())
                .memberCount(memberRepo.countByGroupId(g.getId()))
                .isMember(username != null && memberRepo.existsByGroupIdAndUsername(g.getId(), username))
                .createdAt(g.getCreatedAt())
                .build();
    }

    private GroupChatDTOs.MessageResponse toMessageResponse(GroupMessage msg) {
        Object reactions = new HashMap<>();
        try {
            if (msg.getReactions() != null && !msg.getReactions().equals("{}")) {
                reactions = objectMapper.readValue(msg.getReactions(), Map.class);
            }
        } catch (Exception ignored) {}

        return GroupChatDTOs.MessageResponse.builder()
                .id(msg.getId())
                .groupId(msg.getGroupId())
                .authorName(msg.getAuthorName())
                .content(msg.getContent())
                .reactions(reactions)
                .createdAt(msg.getCreatedAt())
                .build();
    }
}