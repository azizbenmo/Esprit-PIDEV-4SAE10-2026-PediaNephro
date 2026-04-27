package com.pedianephro.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorRequest {
    private Long userId;
    private Integer joursSansConnexion;
    private Integer bilansEnRetard;
    private Integer rappelsIgnores;
    private Integer rendezVousAnnules;
    private Integer medicamentsNonConfirmes;
}
