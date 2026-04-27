package com.pedianephro.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngagementEventRequest {

    @NotNull(message = "userId est requis")
    private Long userId;

    @NotBlank(message = "eventType est requis")
    private String eventType;
}

