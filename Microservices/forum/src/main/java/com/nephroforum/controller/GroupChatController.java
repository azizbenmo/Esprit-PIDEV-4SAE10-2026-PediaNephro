package com.nephroforum.controller;

import com.nephroforum.dto.GroupChatDTOs;
import com.nephroforum.service.GroupChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatService groupService;

    // ── Groupes ───────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<GroupChatDTOs.GroupResponse>> getAll(
            @RequestParam(defaultValue = "") String username) {
        return ResponseEntity.ok(groupService.getAllGroups(username));
    }

    @PostMapping
    public ResponseEntity<GroupChatDTOs.GroupResponse> create(
            @RequestBody GroupChatDTOs.CreateGroupRequest req) {
        return ResponseEntity.ok(groupService.createGroup(req));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> delete(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.ok().build();
    }

    // ── Membres ───────────────────────────────────────────────────────────────
    @PostMapping("/{groupId}/join")
    public ResponseEntity<Void> join(
            @PathVariable Long groupId,
            @RequestBody Map<String, String> body) {
        groupService.joinGroup(groupId, body.get("username"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leave(
            @PathVariable Long groupId,
            @RequestParam String username) {
        groupService.leaveGroup(groupId, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupChatDTOs.MemberResponse>> getMembers(
            @PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getMembers(groupId));
    }

    // ── Messages ──────────────────────────────────────────────────────────────
    @GetMapping("/{groupId}/messages")
    public ResponseEntity<List<GroupChatDTOs.MessageResponse>> getMessages(
            @PathVariable Long groupId,
            @RequestParam String username) {
        return ResponseEntity.ok(groupService.getMessages(groupId, username));
    }

    @PostMapping("/{groupId}/messages")
    public ResponseEntity<GroupChatDTOs.MessageResponse> sendMessage(
            @PathVariable Long groupId,
            @RequestBody GroupChatDTOs.SendMessageRequest req) {
        return ResponseEntity.ok(groupService.sendMessage(groupId, req));
    }

    @PostMapping("/messages/{messageId}/react")
    public ResponseEntity<GroupChatDTOs.MessageResponse> react(
            @PathVariable Long messageId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(groupService.reactToMessage(
                messageId, body.get("username"), body.get("emoji")));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam String username) {
        groupService.deleteMessage(messageId, username);
        return ResponseEntity.ok().build();
    }
}