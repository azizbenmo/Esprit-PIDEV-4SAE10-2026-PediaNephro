package com.example.dossiemedicale.DTO;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstanteResumeItem {
    private Long idConstante;
    private String type;
    private Double valeur;
    private Double seuilMin;
    private Double seuilMax;
    private Date dateMesure;
    private String statut;
}