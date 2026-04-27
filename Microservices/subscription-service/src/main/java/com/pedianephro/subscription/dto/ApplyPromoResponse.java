package com.pedianephro.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyPromoResponse {
    private Boolean valid;
    private String code;
    private Double discountPercent;
    private Double originalPrice;
    private Double discountedPrice;
    private Double savings;
    private String message;
}

