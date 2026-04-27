package com.example.dossiemedicale.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EnfantRequest {

    @NotBlank(message = "nom est obligatoire")
    private String nom;

    @NotBlank(message = "prenom est obligatoire")
    private String prenom;

    private Integer age;

    private String sexe;

    private Double taille;

    private Double poids;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateNaissance;

    @NotNull(message = "patientId est obligatoire")
    private Long patientId;
}