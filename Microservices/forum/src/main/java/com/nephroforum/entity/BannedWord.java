package com.nephroforum.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "banned_words")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannedWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String word;

    @Enumerated(EnumType.STRING)
    private WordType type;

    public enum WordType {
        BAD_WORD, MEDICINE
    }
}