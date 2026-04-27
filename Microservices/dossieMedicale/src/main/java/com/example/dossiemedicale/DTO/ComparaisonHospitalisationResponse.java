package com.example.dossiemedicale.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComparaisonHospitalisationResponse {

    private Long idHospitalisation;

    private PeriodeStats periodeAvant;
    private PeriodeStats periodeApres;

    private String evolution; // AMELIORATION / STABLE / AGGRAVATION
    private String interpretation;
}