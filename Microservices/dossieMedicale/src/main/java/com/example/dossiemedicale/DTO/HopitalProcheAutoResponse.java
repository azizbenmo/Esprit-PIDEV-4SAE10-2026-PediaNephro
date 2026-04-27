package com.example.dossiemedicale.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HopitalProcheAutoResponse {

    private Long idHopital;
    private String nomHopital;
    private String adresse;
    private String ville;
    private String telephone;
    private Boolean urgence;
    private Double latitudeHopital;
    private Double longitudeHopital;
    private Double distanceKm;

    private Double latitudeClient;
    private Double longitudeClient;
    private DateDerniereMaj dateDerniereMaj;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class DateDerniereMaj {
        private String valeur;
    }
}