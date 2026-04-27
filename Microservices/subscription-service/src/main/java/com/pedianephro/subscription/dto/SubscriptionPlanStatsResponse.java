package com.pedianephro.subscription.dto;

import com.pedianephro.subscription.entity.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanStatsResponse {

    private SubscriptionPlan plan;

    /**
     * Nombre total de clients inscrits à ce plan (tous statuts confondus).
     * Si tu veux plus tard ne compter que les abonnements actifs,
     * on pourra adapter la logique.
     */
    private long totalSubscribers;
}

