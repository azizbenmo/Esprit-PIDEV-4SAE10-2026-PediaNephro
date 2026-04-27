package com.nephroforum.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "poll_votes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PollVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String voterName;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private PollOption option;
}