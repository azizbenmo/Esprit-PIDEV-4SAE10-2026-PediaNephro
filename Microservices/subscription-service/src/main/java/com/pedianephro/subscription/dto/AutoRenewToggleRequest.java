package com.pedianephro.subscription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoRenewToggleRequest {

    @NotNull(message = "autoRenew est requis")
    private Boolean autoRenew;
}

