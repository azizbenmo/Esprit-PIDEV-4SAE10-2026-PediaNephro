package com.nephroforum.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "conversation_settings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConversationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Clé unique : "user1_user2" (toujours trié alphabétiquement)
    @Column(unique = true, nullable = false)
    private String conversationKey;

    private String customName;
    private String theme;        // ex: "blue", "purple", "dark"
    private String emoji;        // emoji personnalisé
    private String nickname1;    // pseudo user1
    private String nickname2;    // pseudo user2
    private String photoUrl;
}