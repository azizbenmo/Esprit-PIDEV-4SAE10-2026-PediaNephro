package com.example.dossiemedicale.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class HospitalisationRequest {

    @NotNull(message = "dateEntree est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateEntree;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateSortie;

    private String motif;

    private String serviceHospitalier;

    @NotNull(message = "enfantId est obligatoire")
    private Long enfantId;
}