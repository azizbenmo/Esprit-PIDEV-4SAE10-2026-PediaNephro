package com.example.dossiemedicale.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalisationRequest {

    @NotNull(message = "enfantId est obligatoire")
    private Long enfantId;

    @NotNull(message = "latitude est obligatoire")
    private Double latitude;

    @NotNull(message = "longitude est obligatoire")
    private Double longitude;

    private Double precisionM;

    private String sourceLocalisation;
}