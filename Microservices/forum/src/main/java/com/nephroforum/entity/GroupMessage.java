package com.nephroforum.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private String authorName;

    @Column(nullable = false, length = 1000)
    private String content;

    @Builder.Default
    @Column(length = 500)
    private String reactions = "{}";

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}