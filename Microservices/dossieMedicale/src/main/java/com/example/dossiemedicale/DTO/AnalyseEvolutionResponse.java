package com.example.dossiemedicale.DTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyseEvolutionResponse {
    private Long idDossier;
    private String niveauVigilance;
    private String tendanceGenerale;
    private List<String> pointsPositifs;
    private List<String> pointsVigilance;
    private List<ConstanteEvolutionItem> constantesAnalysees;
    private String conclusion;
}