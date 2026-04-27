package com.example.dossiemedicale.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodeStats {

    private Double temperatureMoyenne;
    private Double poulsMoyen;
    private int nbAlertes;
}