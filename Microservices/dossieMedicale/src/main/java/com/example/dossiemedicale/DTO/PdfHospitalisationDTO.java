package com.example.dossiemedicale.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PdfHospitalisationDTO {
    private String dateEntree;
    private String dateSortie;
    private String motif;
    private String serviceHospitalier;
}