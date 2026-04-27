package com.example.dossiemedicale.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PdfConstanteVitaleDTO {
    private String type;
    private Double valeur;
    private Double seuilMin;
    private Double seuilMax;
    private String dateMesure;
}