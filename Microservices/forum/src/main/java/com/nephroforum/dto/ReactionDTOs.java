package com.nephroforum.dto;

import com.nephroforum.entity.Reaction.ReactionType;
import jakarta.validation.constraints.NotNull;

public class ReactionDTOs {

    public record ReactionRequest(
            Long postId,
            Long commentId,
            @NotNull ReactionType reactionType
    ) {}
}