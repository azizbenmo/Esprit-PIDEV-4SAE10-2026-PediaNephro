package com.pedianephro.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanScore {
    private Long planId;
    private String planName;
    private Double score;
}
