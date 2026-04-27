package com.example.dossiemedicale.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PdfExamenDTO {
    private String type;
    private String resultat;
    private String dateExamen;
}