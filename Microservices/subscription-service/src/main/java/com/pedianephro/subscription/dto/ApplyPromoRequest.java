package com.pedianephro.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyPromoRequest {

    @NotBlank(message = "Le code promo est requis")
    private String code;

    @NotNull(message = "Le prix est requis")
    @Positive(message = "Le prix doit être > 0")
    private Double price;
}

