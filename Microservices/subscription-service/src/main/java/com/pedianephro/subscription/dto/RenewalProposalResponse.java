package com.pedianephro.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RenewalProposalResponse {

    private Long subscriptionId;
    private Long userId;

    private LocalDate currentEndDate;
    private boolean autoRenew;

    private AdjustmentProposalResponse proposal;
}

