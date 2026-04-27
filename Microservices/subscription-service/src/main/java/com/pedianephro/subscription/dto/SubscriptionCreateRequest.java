package com.pedianephro.subscription.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCreateRequest {

    @NotNull
    @Positive
    private Long planId;

    @NotNull
    @Positive
    private Long userId;

    @Size(max = 150)
    private String userFullName;

    /**
     * Durée de l'abonnement en mois (choisie par le patient : 1, 3, 6 ou 12 mois)
     */
    @NotNull
    @Min(1)
    private Integer durationMonths;

    private Boolean autoRenew;

    /**
     * Exemple : "CARD", "BANK_CARD", etc.
     */
    @Size(max = 50)
    private String paymentMethod;

    @NotBlank
    @Email(message = "L'adresse email doit être valide")
    private String userEmail;

    @Size(max = 50)
    private String promoCode;
}
