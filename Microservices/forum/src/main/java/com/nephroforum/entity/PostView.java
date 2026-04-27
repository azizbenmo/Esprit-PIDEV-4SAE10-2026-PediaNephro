package com.nephroforum.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_views",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "viewer_name"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "viewer_name", nullable = false)
    private String viewerName;
}