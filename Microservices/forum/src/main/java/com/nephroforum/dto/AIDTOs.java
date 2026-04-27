package com.nephroforum.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AIDTOs {

    public record DescriptionRequest(@NotBlank String title) {}
    public record DescriptionResponse(String description) {}

    public record TagsRequest(
            @NotBlank String title,
            @NotBlank String description
    ) {}
    public record TagsResponse(List<String> tags) {}

    public record TranslationRequest(
            @NotBlank String text,
            @NotBlank String targetLang
    ) {}
    public record TranslationResponse(String translated) {}
}