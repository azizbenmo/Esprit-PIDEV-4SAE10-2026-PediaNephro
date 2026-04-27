package com.example.dossiemedicale.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DossierMedicalRequest {

    @NotNull(message = "dateCreation est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateCreation;

    @NotNull(message = "enfantId est obligatoire")
    private Long enfantId;
}