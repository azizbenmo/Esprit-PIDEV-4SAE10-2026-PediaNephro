package com.pedianephro.subscription.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeRequest {

    @NotBlank(message = "Le code est requis")
    @Size(max = 50, message = "Le code ne doit pas dépasser 50 caractères")
    private String code;

    @NotNull(message = "Le pourcentage de réduction est requis")
    @Min(value = 0, message = "La réduction doit être >= 0")
    @Max(value = 100, message = "La réduction doit être <= 100")
    private Double discountPercent;

    @NotNull(message = "Le nombre d'utilisations max est requis")
    @Positive(message = "Le nombre d'utilisations max doit être > 0")
    private Integer maxUses;

    @NotNull(message = "La date d'expiration est requise")
    private LocalDate expiryDate;

    private Boolean active;
}

