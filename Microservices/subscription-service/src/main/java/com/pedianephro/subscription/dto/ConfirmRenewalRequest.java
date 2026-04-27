package com.pedianephro.subscription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmRenewalRequest {

    @NotNull(message = "Le choix est requis")
    private Boolean acceptSuggestion;
}

