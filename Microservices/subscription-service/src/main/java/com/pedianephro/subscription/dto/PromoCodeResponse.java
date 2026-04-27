package com.pedianephro.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeResponse {
    private Long id;
    private String code;
    private Double discountPercent;
    private Integer maxUses;
    private Integer currentUses;
    private LocalDate expiryDate;
    private Boolean active;
    private LocalDateTime createdAt;

    private Boolean valid;
    private String message;
}

