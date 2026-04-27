package com.example.dossiemedicale.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstanteEvolutionItem {
    private String type;
    private String tendance;
    private String interpretation;
    private Double derniereValeur;
    private String statutActuel;
}