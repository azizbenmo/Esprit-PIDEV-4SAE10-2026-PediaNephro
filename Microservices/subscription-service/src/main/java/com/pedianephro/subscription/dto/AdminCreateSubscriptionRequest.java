package com.pedianephro.subscription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateSubscriptionRequest {

    @NotNull(message = "userId est requis")
    private Long userId;

    @NotNull(message = "planId est requis")
    private Long planId;

    @NotNull(message = "startDate est requis")
    private LocalDate startDate;

    @NotNull(message = "paymentMethod est requis")
    private String paymentMethod;
}
